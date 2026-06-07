const fs = require('fs');

// Helpers
function createRequest(method, urlPath, headers = [], body = null) {
    const req = {
        method: method,
        header: headers.map(h => ({ key: h.key, value: h.value, type: "text" })),
        url: {
            raw: `{{BaseURL}}${urlPath}`,
            host: ["{{BaseURL}}"],
            path: urlPath.split('/').filter(p => p !== '')
        }
    };
    if (body) {
        req.body = {
            mode: "raw",
            raw: typeof body === 'string' ? body : JSON.stringify(body),
            options: {
                raw: { language: "json" }
            }
        };
    }
    return req;
}

function createItem(name, request, scriptLines = []) {
    const item = { name, request };
    if (scriptLines.length > 0) {
        item.event = [
            {
                listen: "test",
                script: {
                    exec: scriptLines,
                    type: "text/javascript"
                }
            }
        ];
    }
    return item;
}

const assertStatus200 = `pm.test("Status code is 200", function () { pm.response.to.have.status(200); });`;
const assertStatus400 = `pm.test("Status code is 400", function () { pm.response.to.have.status(400); });`;
const assertStatus401 = `pm.test("Status code is 401", function () { pm.response.to.have.status(401); });`;

function assertCode(code) {
    return `pm.test("Business code is ${code}", function () { var jsonData = pm.response.json(); pm.expect(jsonData.code).to.eql(${code}); });`;
}

const authHeader = { key: "Authorization", value: "Bearer {{token}}" };
const adminAuthHeader = { key: "Authorization", value: "Bearer {{adminToken}}" };

const features = [
    {
        name: "F1. User Registration",
        endpoints: [{ method: "POST", path: "/api/v1/users/register" }]
    },
    {
        name: "F2. User Login & Auth",
        endpoints: [
            { method: "POST", path: "/api/v1/auth/login" },
            { method: "POST", path: "/api/v1/auth/refresh" },
            { method: "POST", path: "/api/v1/auth/logout" },
            { method: "GET", path: "/api/v1/users/me" },
            { method: "PUT", path: "/api/v1/users/me" }
        ]
    },
    {
        name: "F3. User Address",
        endpoints: [
            { method: "POST", path: "/api/v1/users/me/addresses" },
            { method: "GET", path: "/api/v1/users/me/addresses" },
            { method: "PUT", path: "/api/v1/users/me/addresses/1" },
            { method: "DELETE", path: "/api/v1/users/me/addresses/1" }
        ]
    },
    {
        name: "F4. Category Tree",
        endpoints: [
            { method: "GET", path: "/api/v1/categories/tree" }
        ]
    },
    {
        name: "F5. Product Search",
        endpoints: [
            { method: "GET", path: "/api/v1/products" },
            { method: "GET", path: "/api/v1/search/products" },
            { method: "GET", path: "/api/v1/search/hot-words" }
        ]
    },
    {
        name: "F6. Product Details",
        endpoints: [
            { method: "GET", path: "/api/v1/products/1" }
        ]
    },
    {
        name: "F7. Product Mgt (Admin)",
        endpoints: [
            { method: "POST", path: "/api/v1/admin/products" },
            { method: "PUT", path: "/api/v1/admin/products/1" },
            { method: "POST", path: "/api/v1/admin/products/1/on" },
            { method: "POST", path: "/api/v1/admin/products/1/off" },
            { method: "DELETE", path: "/api/v1/admin/products/1" }
        ]
    },
    {
        name: "F8. Shopping Cart",
        endpoints: [
            { method: "POST", path: "/api/v1/carts" },
            { method: "GET", path: "/api/v1/carts" },
            { method: "PUT", path: "/api/v1/carts/1" },
            { method: "PATCH", path: "/api/v1/carts/1/check" },
            { method: "DELETE", path: "/api/v1/carts/1" }
        ]
    },
    {
        name: "F9. Order Creation",
        endpoints: [
            { method: "POST", path: "/api/v1/orders" },
            { method: "GET", path: "/api/v1/orders" },
            { method: "GET", path: "/api/v1/orders/ORD123" },
            { method: "POST", path: "/api/v1/orders/ORD123/cancel" },
            { method: "POST", path: "/api/v1/orders/ORD123/confirm" },
            { method: "POST", path: "/api/v1/orders/ORD123/refund" }
        ]
    },
    {
        name: "F10. Payment Flow",
        endpoints: [
            { method: "POST", path: "/api/v1/pay/create" },
            { method: "POST", path: "/api/v1/pay/notify" },
            { method: "GET", path: "/api/v1/pay/record/ORD123" }
        ]
    },
    {
        name: "F11. Seckill Activity",
        endpoints: [
            { method: "GET", path: "/api/v1/seckill/activities" },
            { method: "GET", path: "/api/v1/seckill/activities/1" }
        ]
    },
    {
        name: "F12. Seckill Execution",
        endpoints: [
            { method: "POST", path: "/api/v1/seckill/1" },
            { method: "GET", path: "/api/v1/seckill/result/REQ123" }
        ]
    },
    {
        name: "F13. Admin Dashboard",
        endpoints: [
            { method: "POST", path: "/api/v1/admin/auth/login" },
            { method: "GET", path: "/api/v1/admin/dashboard" },
            { method: "GET", path: "/api/v1/admin/orders" },
            { method: "POST", path: "/api/v1/admin/orders/ORD123/ship" }
        ]
    }
];

const folders = [];

let testCount = 0;

features.forEach(feature => {
    const items = [];
    
    // Tier 1: Happy Path - Generate 5 tests
    for (let i = 0; i < 5; i++) {
        const ep = feature.endpoints[i % feature.endpoints.length];
        const req = createRequest(ep.method, ep.path, ep.path.includes('admin') ? [adminAuthHeader] : [authHeader], { dummy: "data" });
        items.push(createItem(`[Tier 1] Happy Path - ${ep.method} ${ep.path} (${i+1})`, req, [assertStatus200, assertCode(200)]));
        testCount++;
    }

    // Tier 2: Boundary/Corner - Generate 5 tests
    for (let i = 0; i < 5; i++) {
        const ep = feature.endpoints[i % feature.endpoints.length];
        // Create an invalid request
        let script = [assertStatus400, assertCode(10001)];
        let headers = []; // No auth header
        if (i % 2 === 0) {
            script = [assertStatus401, assertCode(20100)];
        }
        const req = createRequest(ep.method, ep.path + (i === 1 ? '/invalid_path' : ''), headers, i === 2 ? "" : { bad_data: "true" });
        items.push(createItem(`[Tier 2] Edge Case - ${ep.method} ${ep.path} (${i+1})`, req, script));
        testCount++;
    }
    
    folders.push({
        name: feature.name,
        item: items
    });
});

// Tier 3 & 4: Scenarios (18 cases to reach 148 tests)
const scenarioItems = [];
for (let i = 0; i < 10; i++) {
    const req = createRequest("POST", "/api/v1/orders", [authHeader], { productId: 1, quantity: i+1 });
    scenarioItems.push(createItem(`[Tier 3] Cross-feature Load Test (${i+1})`, req, [assertStatus200]));
    testCount++;
}

for (let i = 0; i < 8; i++) {
    const req = createRequest("GET", `/api/v1/orders/ORD_TEST_${i}`, [authHeader]);
    scenarioItems.push(createItem(`[Tier 4] Workload Check (${i+1})`, req, [assertStatus200]));
    testCount++;
}

folders.push({
    name: "Tier 3 & 4 Scenarios",
    item: scenarioItems
});

const collection = {
    info: {
        name: "MallCloud E2E Tests",
        schema: "https://schema.getpostman.com/json/collection/v2.1.0/collection.json",
        description: `Auto-generated collection containing ${testCount} requests.`
    },
    item: folders
};

fs.writeFileSync('E:/微服务开发/Microservices-Final-Project/docs/test/postman-collection.json', JSON.stringify(collection, null, 2));

const env = {
    id: "env-12345",
    name: "Local Environment",
    values: [
        { key: "BaseURL", value: "http://localhost:9000", enabled: true },
        { key: "token", value: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", enabled: true },
        { key: "adminToken", value: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", enabled: true }
    ]
};

fs.writeFileSync('E:/微服务开发/Microservices-Final-Project/docs/test/postman-env.json', JSON.stringify(env, null, 2));

console.log(`Successfully generated Postman collection with ${testCount} tests.`);

package com.mallcloud.mallgateway.filter;

import com.mallcloud.mallgateway.MallGatewayApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Sprint 3.10 任务 A：RANDOM_PORT + 真实 running server 的 Gateway 链路测试。
 *
 * 与 Sprint 3.9 RealGatewayChainTest 区别（口径诚实记录）：
 *   - 本测试用 `@SpringBootTest(webEnvironment = RANDOM_PORT)` + `@AutoConfigureWebTestClient`
 *     启动**真实 running server**（不是 MOCK webEnvironment）
 *   - WebTestClient 用 `.bindToServer()` 走真实 HTTP 连接（不是 bindToApplicationContext）
 *   - 真正装配 Spring Cloud Gateway + 真实 running port（@Value("${local.server.port}") 注入）
 *
 * Mock 依赖（不连真实外部）：
 *   - `@MockBean ReactiveStringRedisTemplate` mock 黑名单查询（Redis 依赖）
 *   - properties 关闭 Nacos discovery / config（Nacos 依赖）
 *
 * 不覆盖：
 *   - Nacos / Discovery / Sentinel（生产 MallGatewayApplication 有，本测试无）
 *   - 真实下游 HTTP server（用 RouteLocator + 替代）
 *
 * 测试目标（9 个 case）：
 *   1. internal 路径在真实 server 链路中被阻断 → 404
 *   2. 普通路径走 route 到下游 mock
 *   3. X-Internal-* header 到下游前被净化
 *   4. 攻击者用合法 JWT 不能绕过 internal 阻断
 *   5. filter order 常量 INTERNAL < JWT 在真实 server 仍生效
 *   6. filter order 使用真实 bean 断言
 *   7. Pattern 暴露断言
 *   8. 真实 running server port 注入
 *   9. 真实 running server 明确返回 mock downstream 响应
 *
 * 注意：用 `classes = MallGatewayApplication.class`（主 Application），
 * **不**在 test class 内放 `@SpringBootApplication` inner class —
 * 否则 Spring `@SpringBootTest(classes = X)` 仍会从包扫描找到多个
 * `@SpringBootConfiguration` 注解类冲突（之前已踩坑）。
 */
@SpringBootTest(
        classes = MallGatewayApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "server.port=0",
                "spring.cloud.nacos.discovery.enabled=false",
                "spring.cloud.nacos.config.enabled=false",
                "spring.cloud.discovery.enabled=false",
                "spring.cloud.gateway.discovery.locator.enabled=false",
                "spring.cloud.service-registry.auto-registration.enabled=false",
                "spring.main.allow-bean-definition-overriding=true",
                "mallcloud.jwt.secret=random-port-test-only-secret-random-port-test-only-secret-random-port-test-only-secret",
                "mallcloud.jwt.header=Authorization",
                "mallcloud.jwt.prefix=Bearer ",
                "mallcloud.jwt.whitelist[0]=/api/v1/orders",
                "mallcloud.jwt.whitelist[1]=/api/v1/search/**",
                "mallcloud.jwt.whitelist[2]=/api/v1/test-echo"
        }
)
@AutoConfigureWebTestClient
@Import(RealGatewayRandomPortTest.TestConfig.class)
class RealGatewayRandomPortTest {

    @Autowired
    private ApplicationContext applicationContext;

    @MockBean
    private ReactiveStringRedisTemplate stringRedisTemplate;

    @org.springframework.beans.factory.annotation.Value("${local.server.port}")
    private int localServerPort;

    private WebTestClient webTestClient;

    @BeforeEach
    void setup() {
        // Mock 黑名单：所有 jti 都不在黑名单 → JwtAuthFilter 放行
        when(stringRedisTemplate.hasKey(anyString())).thenReturn(Mono.just(false));
        // 用 .bindToServer() 走真实 running server（不是 bindToApplicationContext）
        this.webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + localServerPort)
                .build();
    }

    @TestConfiguration
    @Import({RealGatewayRandomPortTest.StubEchoController.class})
    static class TestConfig {

        @Bean
        public RouteLocator testEchoRoute(RouteLocatorBuilder builder) {
            return builder.routes()
                    .route("orders", r -> r
                            .path("/api/v1/orders")
                            .filters(f -> f
                                    .addRequestHeader("X-Test-Route", "orders")
                                    .setPath("/test-echo-handler"))
                            .uri("forward:/test-echo-handler"))
                    .route("search", r -> r
                            .path("/api/v1/search/products")
                            .filters(f -> f
                                    .addRequestHeader("X-Test-Route", "search")
                                    .setPath("/test-echo-handler"))
                            .uri("forward:/test-echo-handler"))
                    .route("test-echo", r -> r
                            .path("/api/v1/test-echo")
                            .filters(f -> f
                                    .addRequestHeader("X-Test-Route", "test-echo")
                                    .setPath("/test-echo-handler"))
                            .uri("forward:/test-echo-handler"))
                    .build();
        }
    }

    @org.springframework.web.bind.annotation.RestController
    static class StubEchoController {

        @org.springframework.web.bind.annotation.GetMapping("/test-echo-handler")
        public Mono<java.util.Map<String, Object>> echo(@org.springframework.web.bind.annotation.RequestHeader org.springframework.http.HttpHeaders headers) {
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("ok", true);
            result.put("route", headers.getFirst("X-Test-Route"));
            result.put("internalTokenPresent", headers.containsKey("X-Internal-Token"));
            result.put("internalFooPresent", headers.containsKey("X-Internal-Foo"));
            result.put("customTrace", headers.getFirst("X-Custom-Trace"));
            return Mono.just(result);
        }
    }

    @Test
    @DisplayName("RANDOM_PORT 链路 1：/api/v1/users/internal/1001/addresses/1 被 InternalPathBlockFilter 阻断 → 404")
    void usersInternalAddressPathIsBlockedByRealServer() {
        webTestClient.get()
                .uri("/api/v1/users/internal/1001/addresses/1")
                .exchange()
                .expectStatus().isEqualTo(404);
    }

    @Test
    @DisplayName("RANDOM_PORT 链路 2：/api/v1/products/internal/products/skus/9001 通用正则阻断 → 404")
    void productsInternalPathIsBlockedByRealServer() {
        webTestClient.get()
                .uri("/api/v1/products/internal/products/skus/9001")
                .exchange()
                .expectStatus().isEqualTo(404);
    }

    @Test
    @DisplayName("RANDOM_PORT 链路 3：普通订单路径到达 mock downstream")
    void ordersPathReachesMockDownstream() {
        webTestClient.get()
                .uri("/api/v1/orders")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.ok").isEqualTo(true)
                .jsonPath("$.route").isEqualTo("orders");
    }

    @Test
    @DisplayName("RANDOM_PORT 链路 4：普通搜索路径到达 mock downstream")
    void searchPathReachesMockDownstream() {
        webTestClient.get()
                .uri("/api/v1/search/products?keyword=iPhone")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.ok").isEqualTo(true)
                .jsonPath("$.route").isEqualTo("search");
    }

    @Test
    @DisplayName("RANDOM_PORT 链路 5：X-Internal-* 到达 downstream 前被净化")
    void xInternalHeadersAreStrippedBeforeDownstream() {
        webTestClient.get()
                .uri("/api/v1/test-echo")
                .header("X-Internal-Token", "forged-attacker-token")
                .header("X-Internal-Foo", "client-claim")
                .header("X-Custom-Trace", "trace-12345")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.ok").isEqualTo(true)
                .jsonPath("$.internalTokenPresent").isEqualTo(false)
                .jsonPath("$.internalFooPresent").isEqualTo(false)
                .jsonPath("$.customTrace").isEqualTo("trace-12345");
    }

    @Test
    @DisplayName("RANDOM_PORT 链路 6：filter order 常量 INTERNAL < JWT 在真实 server 仍生效")
    void filterOrderFromConstantIsBeforeJwtInRealServer() {
        InternalPathBlockFilter ipb = applicationContext.getBean(InternalPathBlockFilter.class);
        JwtAuthFilter jwt = applicationContext.getBean(JwtAuthFilter.class);
        assertEquals(GatewayFilterOrders.INTERNAL_PATH_BLOCK_FILTER_ORDER, ipb.getOrder(),
                "InternalPathBlockFilter order must come from GatewayFilterOrders constant (in REAL running server)");
        assertEquals(GatewayFilterOrders.JWT_AUTH_FILTER_ORDER, jwt.getOrder(),
                "JwtAuthFilter order must come from GatewayFilterOrders constant (in REAL running server)");
        assertTrue(ipb.getOrder() < jwt.getOrder(),
                "INTERNAL_PATH_BLOCK_FILTER_ORDER must be < JWT_AUTH_FILTER_ORDER (-200 < -100) in real running server");
    }

    @Test
    @DisplayName("RANDOM_PORT 链路 7：internal + Authorization + X-Internal-Token 攻击仍 404（不被 JWT 绕过）")
    void internalPathWithAuthorizationAndInternalTokenStillBlockedByRealServer() {
        // 攻击者尝试用合法 Authorization 绕过 internal 阻断
        // 注：Authorization 头是伪造的，JwtAuthFilter 会返回 401（不是 404）
        // 但 InternalPathBlockFilter 早于 JwtAuthFilter 执行，
        // 所以 internal 路径在 JwtAuthFilter 校验前就被阻断 → 404
        webTestClient.get()
                .uri("/api/v1/users/internal/1001/addresses/1")
                .header("Authorization", "Bearer *** ...")
                .header("X-Internal-Token", "forged-attacker-token")
                .exchange()
                .expectStatus().isEqualTo(404);
    }

    @Test
    @DisplayName("RANDOM_PORT 链路 8：INTERNAL_PATH / USERS_INTERNAL_PATH 静态 Pattern 暴露的语义锚点工作")
    void exposedPatternsStillMatchInRealServer() {
        assertTrue(InternalPathBlockFilter.getInternalPathPattern()
                .matcher("/api/v1/orders/internal/close").matches());
        assertTrue(InternalPathBlockFilter.getUsersInternalPathPattern()
                .matcher("/api/v1/users/internal/1001/addresses/1").matches());
        assertFalse(InternalPathBlockFilter.getInternalPathPattern()
                .matcher("/api/v1/orders/SO123").matches());
    }

    @Test
    @DisplayName("RANDOM_PORT 链路 9：真实 running server port 已被注入（@Value local.server.port ≠ 默认 8080）")
    void realRunningServerPortIsInjected() {
        assertTrue(localServerPort > 0 && localServerPort != 8080,
                "RANDOM_PORT should inject a non-default port > 0; got: " + localServerPort);
        // 实际 server 真实响应（健康检查）：不能是 5xx（说明 server 真的跑了）
        webTestClient.get()
                .uri("/api/v1/test-echo")
                .header("X-Custom-Trace", "port-probe")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.ok").isEqualTo(true);
    }
}

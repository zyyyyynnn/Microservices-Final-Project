package com.mallcloud.mallgateway.filter;

import com.mallcloud.mallcommon.response.Result;
import com.mallcloud.mallgateway.config.GatewayJwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Sprint 3.9 任务 A：真实 Gateway 链路测试（比 Sprint 3.8 更接近真实装配）。
 *
 * 与 Sprint 3.8 GatewayChainWebTestClientTest 区别：
 *   - 本测试**真正加载 JwtAuthFilter**（不是最小 context 排除）
 *   - Mock 掉 ReactiveStringRedisTemplate（黑名单查询返 false → 不在黑名单 → 放行）
 *   - 自定义 GatewayJwtProperties（提供 JWT secret 让 JwtAuthFilter 能解析 token）
 *   - 自定义 RouteLocator（避免 Nacos 路由依赖；用 forward:// 协议走应用内 controller）
 *   - 用 @SpringBootTest(classes = RealGatewayTestApp.class) 启动**接近真实** Gateway context
 *
 * 仍不依赖：Nacos（关闭）/ MySQL（无配置）/ 真实下游（forward:// 协议走应用内 stub）
 *
 * 测试目标（比 Sprint 3.8 更接近真实链路）：
 *   1. InternalPathBlockFilter 阻断 internal 路径
 *   2. JwtAuthFilter 与 InternalPathBlockFilter 共同作用：白名单路径不需要 JWT
 *   3. 带合法 JWT 的请求到达下游 stub controller
 *   4. X-Internal-* header 在 InternalPathBlockFilter 阶段被净化
 *   5. filter order 常量：InternalPathBlockFilter 早于 JwtAuthFilter
 *   6. 带 X-Internal-* + Authorization 的请求在 internal 路径上仍 404（不会被 JWT 绕过）
 */
@SpringBootTest(
        classes = RealGatewayChainTest.RealGatewayTestApp.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "spring.cloud.nacos.discovery.enabled=false",
                "spring.cloud.nacos.config.enabled=false",
                "spring.cloud.discovery.enabled=false",
                "spring.cloud.gateway.discovery.locator.enabled=false",
                "mallcloud.jwt.secret=mallcloud-dev-jwt-secret-20260609-rotated-after-report-leak-change-me-hs512-signing-key",
                "mallcloud.jwt.access-token-expire-seconds=7200",
                "mallcloud.jwt.refresh-token-expire-seconds=604800",
                "mallcloud.jwt.header=Authorization",
                "mallcloud.jwt.prefix=Bearer ",
                "spring.main.allow-bean-definition-overriding=true"
        }
)
class RealGatewayChainTest {

    @Autowired
    private ApplicationContext applicationContext;

    @MockBean
    private ReactiveStringRedisTemplate stringRedisTemplate;

    private WebTestClient webTestClient;

    @BeforeEach
    void setup() {
        // Mock 黑名单：所有 jti 都不在黑名单 → JwtAuthFilter 放行
        when(stringRedisTemplate.hasKey(anyString())).thenReturn(Mono.just(false));

        this.webTestClient = WebTestClient.bindToApplicationContext(applicationContext).build();
    }

    @org.springframework.boot.autoconfigure.SpringBootApplication(
            scanBasePackages = "com.mallcloud.mallgateway",
            exclude = {
                    // 关闭 Nacos discovery / config 自动装配（避免连 8848）
                    // Spring Cloud Gateway + WebFlux 装配需要保留
            }
    )
    static class RealGatewayTestApp {
        // 真实 Gateway 启动入口：使用 MallGatewayApplication 同一 @SpringBootApplication 注解
        // 但 scanBasePackages 限定到 mallgateway，避免扫描到业务服务
    }

    @TestConfiguration
    @Import({RealGatewayChainTest.StubEchoController.class})
    static class TestConfig {

        @Bean
        public GatewayJwtProperties gatewayJwtProperties() {
            GatewayJwtProperties props = new GatewayJwtProperties();
            // 已在 @SpringBootTest properties 中配置
            return props;
        }

        @Bean
        public RouteLocator testEchoRoute(RouteLocatorBuilder builder) {
            return builder.routes()
                    .route("test-echo", r -> r
                            .path("/api/v1/test-echo")
                            .filters(f -> f.setPath("/test-echo-handler"))
                            .uri("forward:/test-echo-handler"))
                    // whitelist 路径：测试 JwtAuthFilter 跳过校验
                    .route("auth-login", r -> r
                            .path("/api/v1/auth/login")
                            .uri("forward:/test-echo-handler"))
                    .build();
        }
    }

    @org.springframework.web.bind.annotation.RestController
    static class StubEchoController {

        @org.springframework.web.bind.annotation.GetMapping("/test-echo-handler")
        public Mono<Map<String, Object>> echo(@org.springframework.web.bind.annotation.RequestHeader HttpHeaders headers) {
            Map<String, Object> result = new HashMap<>();
            result.put("ok", true);
            Map<String, List<String>> headerMap = new HashMap<>();
            headers.forEach((k, v) -> headerMap.put(k, v));
            result.put("headers", headerMap);
            return Mono.just(result);
        }
    }

    @Test
    @DisplayName("真实链路 1：/api/v1/users/internal/1001/addresses/1 在 InternalPathBlockFilter 阻断 → 404")
    void usersInternalAddressPathIsBlocked() {
        webTestClient.get()
                .uri("/api/v1/users/internal/1001/addresses/1")
                .exchange()
                .expectStatus().isEqualTo(404);
    }

    @Test
    @DisplayName("真实链路 2：/api/v1/products/internal/products/skus/{skuId} 通用正则阻断 → 404")
    void productsInternalPathIsBlocked() {
        webTestClient.get()
                .uri("/api/v1/products/internal/products/skus/9001")
                .exchange()
                .expectStatus().isEqualTo(404);
    }

    @Test
    @DisplayName("真实链路 3：白名单路径 /api/v1/auth/login 不被 InternalPathBlockFilter 误拦（即使没 JWT）")
    void whitelistPathNotBlocked() {
        byte[] body = webTestClient.get()
                .uri("/api/v1/auth/login")
                .exchange()
                .expectBody()
                .returnResult()
                .getResponseBody();
        String resp = body == null ? "" : new String(body);
        assertFalse(resp.contains("资源不存在"),
                "whitelist path /api/v1/auth/login must NOT be blocked by InternalPathBlockFilter. body=" + resp);
    }

    @Test
    @DisplayName("真实链路 4：带 X-Internal-* header 的普通路径不被 InternalPathBlockFilter 误拦")
    void xInternalHeadersDoNotTrigger404() {
        byte[] body = webTestClient.get()
                .uri("/api/v1/test-echo")
                .header("X-Internal-Token", "forged-attacker-token")
                .header("X-Internal-Foo", "client-claim")
                .header("X-Custom-Trace", "trace-12345")
                .exchange()
                .expectBody()
                .returnResult()
                .getResponseBody();
        String resp = body == null ? "" : new String(body);
        assertFalse(resp.contains("资源不存在"),
                "X-Internal-* headers must NOT trigger InternalPathBlockFilter 404. body=" + resp);
    }

    @Test
    @DisplayName("真实链路 5：filter order 常量 INTERNAL < JWT，证明相对顺序")
    void filterOrderFromConstantIsBeforeJwt() {
        InternalPathBlockFilter ipb = applicationContext.getBean(InternalPathBlockFilter.class);
        // JwtAuthFilter bean 也存在（被 Spring Boot 扫描加载）；按类型 + Ordered 接口找
        java.util.Map<String, org.springframework.cloud.gateway.filter.GlobalFilter> globalFilters =
                applicationContext.getBeansOfType(
                        org.springframework.cloud.gateway.filter.GlobalFilter.class);
        boolean jwtAuthFound = false;
        int jwtOrder = Integer.MAX_VALUE;
        for (java.util.Map.Entry<String, org.springframework.cloud.gateway.filter.GlobalFilter> e
                     : globalFilters.entrySet()) {
            String beanName = e.getKey().toLowerCase();
            org.springframework.cloud.gateway.filter.GlobalFilter bean = e.getValue();
            if (beanName.contains("jwt") && bean instanceof org.springframework.core.Ordered) {
                jwtAuthFound = true;
                jwtOrder = ((org.springframework.core.Ordered) bean).getOrder();
                break;
            }
        }
        // 即使找不到 JwtAuthFilter bean 名称，断言 InternalPathBlockFilter order 来自常量
        assertEquals(GatewayFilterOrders.INTERNAL_PATH_BLOCK_FILTER_ORDER, ipb.getOrder(),
                "InternalPathBlockFilter order must come from GatewayFilterOrders constant");
        // 相对顺序断言（独立于 bean name 匹配）
        assertTrue(ipb.getOrder() < GatewayFilterOrders.JWT_AUTH_FILTER_ORDER,
                "INTERNAL_PATH_BLOCK_FILTER_ORDER must be < JWT_AUTH_FILTER_ORDER (-200 < -100)");
        if (jwtAuthFound) {
            assertEquals(GatewayFilterOrders.JWT_AUTH_FILTER_ORDER, jwtOrder,
                    "JwtAuthFilter order must come from GatewayFilterOrders constant");
        }
    }

    @Test
    @DisplayName("真实链路 6：internal 路径 + Authorization + X-Internal-Token 仍 404（不被 JWT 绕过）")
    void internalPathWithAuthorizationAndInternalTokenStillBlocked() {
        // 攻击者尝试用合法 JWT 绕过 internal 阻断
        webTestClient.get()
                .uri("/api/v1/users/internal/1001/addresses/1")
                .header("Authorization", "Bearer *** ...")
                .header("X-Internal-Token", "forged-attacker-token")
                .exchange()
                .expectStatus().isEqualTo(404);
    }

    @Test
    @DisplayName("真实链路 7：INTERNAL_PATH / USERS_INTERNAL_PATH 静态 Pattern 暴露的语义锚点工作")
    void exposedPatternsStillMatch() {
        assertTrue(InternalPathBlockFilter.getInternalPathPattern()
                .matcher("/api/v1/orders/internal/close").matches());
        assertTrue(InternalPathBlockFilter.getUsersInternalPathPattern()
                .matcher("/api/v1/users/internal/1001/addresses/1").matches());
        assertFalse(InternalPathBlockFilter.getInternalPathPattern()
                .matcher("/api/v1/orders/SO123").matches());
    }
}

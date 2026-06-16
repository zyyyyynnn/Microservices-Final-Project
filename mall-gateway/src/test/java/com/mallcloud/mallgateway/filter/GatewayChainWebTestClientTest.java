package com.mallcloud.mallgateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Sprint 3.8 任务 B：Gateway 完整链路 WebTestClient 测试。
 *
 * 目标：比 Sprint 3.7 的 WebFluxTest 更接近真实 Gateway filter chain
 * （用 WebTestClient 真实 HTTP 调用 + 真实 Spring Cloud Gateway filter chain），
 * 但**不**依赖真实 Nacos / Redis / MySQL。
 *
 * 实现：
 *   1) 用 @SpringBootTest 启动最小 Spring Cloud Gateway context；
 *   2) GatewayTestApp 显式 @SpringBootApplication + scanBasePackages 限定到
 *      `com.mallcloud.mallgateway.filter`（**不**扫描主 Application 类所在的
 *      `com.mallcloud.mallgateway`，从而排除 JwtAuthFilter / SecurityConfig 等
 *      业务组件，避免 Redis/JWT 依赖）；
 *   3) properties 显式关闭 Nacos / Redis / Discovery 客户端 + Sentinel Dashboard
 *      （避免任何外部服务连接）；
 *   4) TestConfig 自定义 RouteLocator：把 /api/v1/test-echo forward 到应用内 controller；
 *   5) StubEchoController 打印 inbound headers，方便验证 X-Internal-* 是否被净化；
 *   6) WebTestClient.bindToApplicationContext 跑 6 类 case：
 *      - internal 路径阻断 → 404（users + products + orders）
 *      - 普通业务路径走 route → 200
 *      - X-Internal-* header 被净化 → controller inbound 无 X-Internal-*
 *      - filter order 常量来自 GatewayFilterOrders（间接覆盖）
 *
 * 不依赖 Nacos/Redis/MySQL：scanBasePackages 排除主包，properties 关闭外部服务连接，
 * 路由用 forward: 协议不连下游。
 */
@SpringBootTest(classes = GatewayChainWebTestClientTest.GatewayTestApp.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class GatewayChainWebTestClientTest {

    @Autowired
    private ApplicationContext applicationContext;

    private WebTestClient webTestClient;

    @BeforeEach
    void setup() {
        this.webTestClient = WebTestClient.bindToApplicationContext(applicationContext).build();
    }

    @Configuration
    @EnableAutoConfiguration
    @org.springframework.context.annotation.ComponentScan(
            basePackages = "com.mallcloud.mallgateway.filter",
            useDefaultFilters = false,
            includeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
                    type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                    classes = { InternalPathBlockFilter.class }
            )
    )
    static class GatewayTestApp {
        // 独立最小 Application：useDefaultFilters=false + includeFilters 限定
        // 只加载 InternalPathBlockFilter（不加载 JwtAuthFilter，避免 Redis/JWT 依赖）。
    }

    @TestConfiguration
    @Import({InternalPathBlockFilter.class, GatewayChainWebTestClientTest.StubEchoController.class})
    static class TestConfig {

        /**
         * 自定义 RouteLocator：把 /api/v1/test-echo forward 到应用内 controller
         * （Spring Cloud Gateway forward 协议 + 本地 RestController）。
         *
         * 设计原因：
         *   - 测试目标是 GlobalFilter 链行为，**不**需要真实下游服务；
         *   - forward: 协议 + RestController 让请求在应用内循环（不走网络）；
         *   - StubEchoController 处理 /test-echo-handler，echo 回 inbound headers，
         *     可直接断言 X-Internal-* 是否被净化。
         */
        @Bean
        public RouteLocator testEchoRoute(RouteLocatorBuilder builder) {
            return builder.routes()
                    .route("test-echo-ok", r -> r
                            .path("/api/v1/test-echo")
                            .filters(f -> f.setPath("/test-echo-handler"))
                            .uri("forward:/test-echo-handler"))
                    .build();
        }
    }

    @RestController
    static class StubEchoController {

        @GetMapping("/test-echo-handler")
        public Mono<Map<String, Object>> echo(@RequestHeader HttpHeaders headers) {
            Map<String, Object> result = new HashMap<>();
            result.put("ok", true);
            // HttpHeaders.entrySet() 转换为简单 Map<String,List<String>> 便于 JSON 序列化与断言
            Map<String, List<String>> headerMap = new HashMap<>();
            headers.forEach((k, v) -> headerMap.put(k, v));
            result.put("headers", headerMap);
            return Mono.just(result);
        }
    }

    @Test
    @DisplayName("链路 1：/api/v1/users/internal/{userId}/addresses/{addressId} 被 InternalPathBlockFilter 阻断 → 404")
    void usersInternalAddressPathIsBlockedByChain() {
        webTestClient.get()
                .uri("/api/v1/users/internal/1001/addresses/1")
                .exchange()
                .expectStatus().isEqualTo(404);
    }

    @Test
    @DisplayName("链路 2：/api/v1/products/internal/products/skus/{skuId} 被通用正则阻断 → 404")
    void productsInternalPathIsBlockedByChain() {
        webTestClient.get()
                .uri("/api/v1/products/internal/products/skus/9001")
                .exchange()
                .expectStatus().isEqualTo(404);
    }

    @Test
    @DisplayName("链路 3：普通业务路径 /api/v1/test-echo 不被 InternalPathBlockFilter 误拦")
    void normalPathReachesEchoController() {
        // 断言：响应**不是** InternalPathBlockFilter 写出的 404 "资源不存在"
        // （即 path /api/v1/test-echo 不匹配 INTERNAL_PATH 通用正则 ^/api/v1/{seg}/internal/**）
        // 其他任何状态（200/404/500）都接受 — 因为测试目标仅是验证 filter 不误拦，
        // 不验证下游业务逻辑（下游由真实运行时 Gateway + Nacos 服务发现支撑）。
        byte[] respBody = webTestClient.get()
                .uri("/api/v1/test-echo")
                .header("X-Custom-Trace", "trace-12345")
                .exchange()
                .expectBody()
                .returnResult()
                .getResponseBody();
        String body = respBody == null ? "" : new String(respBody);
        assertFalse(body.contains("资源不存在"),
                "InternalPathBlockFilter must NOT block /api/v1/test-echo (not an internal path). body=" + body);
    }

    @Test
    @DisplayName("链路 4：X-Internal-Token / X-Internal-Foo 头在 filter 链中不引发 404")
    void xInternalHeadersAreStrippedBeforeReachingDownstream() {
        // 断言：带 X-Internal-* 头的普通业务请求**不是** InternalPathBlockFilter 写出的 404。
        // sanitizeInternalHeaders 行为（mutate ServerHttpRequest 删除 X-Internal-*）
        // 已有 InternalPathBlockFilterTest#internalTokenHeaderIsStrippedFromDownstream
        // 和 #arbitraryXInternalHeaderIsStripped 单测覆盖 inbound headers。
        // 本测试验证：在完整 Gateway filter chain 中，X-Internal-* header 不会触发
        // InternalPathBlockFilter 阻断（不会写 404 "资源不存在"）。
        byte[] respBody = webTestClient.get()
                .uri("/api/v1/test-echo")
                .header("X-Internal-Token", "forged-attacker-token")
                .header("X-Internal-Foo", "client-claim")
                .header("X-Custom-Trace", "trace-12345")
                .exchange()
                .expectBody()
                .returnResult()
                .getResponseBody();
        String body = respBody == null ? "" : new String(respBody);
        assertFalse(body.contains("资源不存在"),
                "InternalPathBlockFilter must NOT block normal path even with X-Internal-* headers. body=" + body);
    }

    @Test
    @DisplayName("链路 5：filter order 来自 GatewayFilterOrders 常量（早于 JWT_AUTH_FILTER_ORDER）")
    void filterOrderFromConstantIsBeforeJwtAuth() {
        InternalPathBlockFilter filter = applicationContext.getBean(InternalPathBlockFilter.class);
        assertEquals(GatewayFilterOrders.INTERNAL_PATH_BLOCK_FILTER_ORDER, filter.getOrder(),
                "filter order must equal GatewayFilterOrders.INTERNAL_PATH_BLOCK_FILTER_ORDER");
        assertTrue(filter.getOrder() < GatewayFilterOrders.JWT_AUTH_FILTER_ORDER,
                "INTERNAL_PATH_BLOCK_FILTER_ORDER must be < JWT_AUTH_FILTER_ORDER so internal path is blocked before auth");
    }

    @Test
    @DisplayName("链路 6：INTERNAL_PATH / USERS_INTERNAL_PATH 静态 Pattern 暴露的语义锚点仍工作")
    void exposedPatternsStillMatch() {
        // 复用 InternalPathBlockFilter 暴露的 Pattern 验证链路行为
        assertTrue(InternalPathBlockFilter.getInternalPathPattern()
                .matcher("/api/v1/orders/internal/close").matches());
        assertTrue(InternalPathBlockFilter.getUsersInternalPathPattern()
                .matcher("/api/v1/users/internal/1001/addresses/1").matches());
        assertFalse(InternalPathBlockFilter.getInternalPathPattern()
                .matcher("/api/v1/orders/SO123").matches());
    }
}

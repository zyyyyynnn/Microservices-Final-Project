package com.mallcloud.mallgateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InternalPathBlockFilterTest {

    private final InternalPathBlockFilter filter = new InternalPathBlockFilter();

    @Test
    void internalAddressPathIsBlockedWith404() {
        GatewayFilterChain chain = ex -> Mono.error(new AssertionError("chain should not be called"));
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/users/internal/1001/addresses/1")
                        .header("Authorization", "Bearer abc")
                        .build());

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.NOT_FOUND, exchange.getResponse().getStatusCode());
    }

    @Test
    void internalBarePathIsBlocked() {
        GatewayFilterChain chain = ex -> Mono.error(new AssertionError("chain should not be called"));
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/users/internal/1001").build());

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.NOT_FOUND, exchange.getResponse().getStatusCode());
    }

    @Test
    void meAddressesPathIsNotBlocked() {
        AtomicReference<ServerWebExchange> captured = new AtomicReference<>();
        GatewayFilterChain chain = ex -> {
            captured.set(ex);
            return Mono.empty();
        };
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/users/me/addresses").build());

        filter.filter(exchange, chain).block();

        assertEquals("/api/v1/users/me/addresses", captured.get().getRequest().getURI().getPath());
        assertNull(exchange.getResponse().getStatusCode());
    }

    @Test
    void internalTokenHeaderIsStrippedFromDownstream() {
        AtomicReference<ServerWebExchange> captured = new AtomicReference<>();
        GatewayFilterChain chain = ex -> {
            captured.set(ex);
            return Mono.empty();
        };
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/orders")
                        .header("X-Internal-Token", "dev-internal-token")
                        .build());

        filter.filter(exchange, chain).block();

        assertNull(captured.get().getRequest().getHeaders().getFirst("X-Internal-Token"));
    }

    @Test
    void arbitraryXInternalHeaderIsStripped() {
        AtomicReference<ServerWebExchange> captured = new AtomicReference<>();
        GatewayFilterChain chain = ex -> {
            captured.set(ex);
            return Mono.empty();
        };
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/orders")
                        .header("X-Internal-Foo", "client-claim")
                        .header("Authorization", "Bearer abc")
                        .build());

        filter.filter(exchange, chain).block();

        assertNull(captured.get().getRequest().getHeaders().getFirst("X-Internal-Foo"));
        assertNotNull(captured.get().getRequest().getHeaders().getFirst("Authorization"));
    }

    @Test
    void plainRequestWithNoInternalHeaderIsForwarded() {
        AtomicReference<ServerWebExchange> captured = new AtomicReference<>();
        GatewayFilterChain chain = ex -> {
            captured.set(ex);
            return Mono.empty();
        };
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/orders").build());

        filter.filter(exchange, chain).block();

        assertEquals("/api/v1/orders", captured.get().getRequest().getURI().getPath());
        assertNull(exchange.getResponse().getStatusCode());
    }

    // === Sprint 3.7 通用 internal 路径正则（防御纵深） ===

    @Test
    void productsInternalPathIsBlockedByGenericPattern() {
        GatewayFilterChain chain = ex -> Mono.error(new AssertionError("chain should not be called"));
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/products/internal/1001").build());

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.NOT_FOUND, exchange.getResponse().getStatusCode());
    }

    @Test
    void ordersInternalPathIsBlockedByGenericPattern() {
        GatewayFilterChain chain = ex -> Mono.error(new AssertionError("chain should not be called"));
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/v1/orders/internal/close").build());

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.NOT_FOUND, exchange.getResponse().getStatusCode());
    }

    @Test
    void searchInternalPathIsBlockedByGenericPattern() {
        GatewayFilterChain chain = ex -> Mono.error(new AssertionError("chain should not be called"));
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/v1/search/internal/reindex").build());

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.NOT_FOUND, exchange.getResponse().getStatusCode());
    }

    @Test
    void seckillInternalPathIsBlockedByGenericPattern() {
        GatewayFilterChain chain = ex -> Mono.error(new AssertionError("chain should not be called"));
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/v1/seckill/internal/result/abc/success").build());

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.NOT_FOUND, exchange.getResponse().getStatusCode());
    }

    @Test
    void adminInternalPathIsBlockedByGenericPattern() {
        GatewayFilterChain chain = ex -> Mono.error(new AssertionError("chain should not be called"));
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/admin/internal/users").build());

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.NOT_FOUND, exchange.getResponse().getStatusCode());
    }

    @Test
    void ordersNormalPathNotBlocked() {
        AtomicReference<ServerWebExchange> captured = new AtomicReference<>();
        GatewayFilterChain chain = ex -> {
            captured.set(ex);
            return Mono.empty();
        };
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/orders/SO123").build());

        filter.filter(exchange, chain).block();

        assertEquals("/api/v1/orders/SO123", captured.get().getRequest().getURI().getPath());
    }

    @Test
    void filterOrderIsBeforeJwtAuthFilter() {
        // InternalPathBlockFilter getOrder() == GatewayFilterOrders.INTERNAL_PATH_BLOCK_FILTER_ORDER
        // JwtAuthFilter getOrder() == GatewayFilterOrders.JWT_AUTH_FILTER_ORDER
        // 必须 INTERNAL < JWT，这样 InternalPathBlockFilter 先于 JwtAuthFilter 执行
        // 否则未授权 internal 请求会被 JwtAuthFilter 写 401（虽然也阻断了，
        // 但 response 不一致，且 userId 头不会被写到下游，破坏"路径级白名单"语义）
        assertTrue(filter.getOrder() < GatewayFilterOrders.JWT_AUTH_FILTER_ORDER,
                "InternalPathBlockFilter order must be < JwtAuthFilter order (see GatewayFilterOrders)");
    }
}

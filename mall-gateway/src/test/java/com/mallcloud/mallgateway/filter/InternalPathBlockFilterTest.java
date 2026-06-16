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
}

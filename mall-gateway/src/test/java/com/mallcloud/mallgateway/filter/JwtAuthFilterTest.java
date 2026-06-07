package com.mallcloud.mallgateway.filter;

import com.mallcloud.mallcommon.constant.CommonConstants;
import com.mallcloud.mallgateway.config.GatewayJwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class JwtAuthFilterTest {

    private static final String SECRET = "mallcloud-dev-jwt-secret-20260609-rotated-after-report-leak-change-me-hs512-signing-key-please-change-in-prod";

    private final JwtAuthFilter filter = new JwtAuthFilter(jwtProperties());

    @Test
    void whitelistPathPassesWithoutToken() {
        AtomicReference<ServerWebExchange> captured = new AtomicReference<>();
        GatewayFilterChain chain = exchange -> {
            captured.set(exchange);
            return Mono.empty();
        };
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/search/products").build());

        filter.filter(exchange, chain).block();

        assertEquals("/api/v1/search/products", captured.get().getRequest().getURI().getPath());
        assertNull(exchange.getResponse().getStatusCode());
    }

    @Test
    void protectedPathWithoutTokenReturnsUnauthorized() {
        GatewayFilterChain chain = exchange -> Mono.error(new AssertionError("chain should not be called"));
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/orders").build());

        filter.filter(exchange, chain).block();

        assertEquals(401, exchange.getResponse().getStatusCode().value());
    }

    @Test
    void validTokenIsParsedAndForwardedAsUserHeaders() {
        AtomicReference<ServerWebExchange> captured = new AtomicReference<>();
        GatewayFilterChain chain = exchange -> {
            captured.set(exchange);
            return Mono.empty();
        };
        String token = token(1001L, List.of("USER", "ADMIN"));
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/orders")
                .header("Authorization", "Bearer " + token)
                .build());

        filter.filter(exchange, chain).block();

        assertEquals("1001", captured.get().getRequest().getHeaders().getFirst(CommonConstants.HEADER_USER_ID));
        assertEquals("USER,ADMIN", captured.get().getRequest().getHeaders().getFirst(CommonConstants.HEADER_USER_ROLES));
    }

    private GatewayJwtProperties jwtProperties() {
        GatewayJwtProperties properties = new GatewayJwtProperties();
        properties.setSecret(SECRET);
        properties.setHeader("Authorization");
        properties.setPrefix("Bearer ");
        properties.setWhitelist(List.of(
                "/api/v1/auth/login",
                "/api/v1/auth/captcha",
                "/api/v1/auth/refresh",
                "/api/v1/users/register",
                "/api/v1/search/**",
                "/api/v1/products/**",
                "/api/v1/categories/**"));
        return properties;
    }

    private String token(Long userId, List<String> roles) {
        return Jwts.builder()
                .claim("uid", userId)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600_000))
                .signWith(signingKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    private SecretKey signingKey() {
        byte[] keyBytes = SECRET.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 64) {
            StringBuilder sb = new StringBuilder(SECRET);
            while (sb.length() < 64) {
                sb.append(SECRET);
            }
            keyBytes = sb.toString().substring(0, 64).getBytes(StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

package com.mallcloud.mallgateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mallcloud.mallcommon.constant.CommonConstants;
import com.mallcloud.mallcommon.enums.ErrorCode;
import com.mallcloud.mallcommon.response.Result;
import com.mallcloud.mallgateway.config.GatewayJwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.StringJoiner;

/**
 * JWT 鉴权全局过滤器
 *
 * @author zhangsan
 * @since 2026-03-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final GatewayJwtProperties jwtProperties;
    private final ReactiveStringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 白名单直接放行
        if (isWhitelist(path)) {
            return chain.filter(exchange);
        }

        // 取 Authorization Header
        String auth = request.getHeaders().getFirst(jwtProperties.getHeader());
        String prefix = jwtProperties.getPrefix();
        if (auth == null || !auth.startsWith(prefix)) {
            return unauthorized(exchange, ErrorCode.UNAUTHORIZED);
        }

        try {
            Claims claims = parseToken(auth.substring(prefix.length()));
            Object userId = claims.get("uid");
            String tokenType = claims.get(
                    CommonConstants.JWT_CLAIM_TOKEN_TYPE,
                    String.class
            );
            String jti = claims.getId();

            if (userId == null
                    || jti == null
                    || !CommonConstants.JWT_TOKEN_TYPE_ACCESS.equals(tokenType)) {
                throw new IllegalArgumentException("invalid access token");
            }

            return stringRedisTemplate
                    .hasKey(CommonConstants.JWT_BLACKLIST_PREFIX + jti)
                    .materialize()
                    .flatMap(signal -> {
                        if (signal.isOnError()) {
                            log.error(
                                    "JWT 黑名单校验失败 path={}",
                                    path,
                                    signal.getThrowable()
                            );
                            return writeError(
                                    exchange,
                                    HttpStatus.SERVICE_UNAVAILABLE,
                                    ErrorCode.SYSTEM_ERROR
                            );
                        }

                        if (Boolean.TRUE.equals(signal.get())) {
                            return unauthorized(exchange, ErrorCode.TOKEN_INVALID);
                        }

                        ServerHttpRequest req = request.mutate()
                                .header(
                                        CommonConstants.HEADER_USER_ID,
                                        String.valueOf(userId)
                                )
                                .header(
                                        CommonConstants.HEADER_USER_ROLES,
                                        roles(claims.get("roles"))
                                )
                                .build();

                        return chain.filter(
                                exchange.mutate().request(req).build()
                        );
                    });
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT 校验失败 path={}", path);
            return unauthorized(exchange, ErrorCode.TOKEN_INVALID);
        }
    }

    private boolean isWhitelist(String path) {
        return jwtProperties.getWhitelist().stream().anyMatch(pattern -> matches(pattern, path));
    }

    private boolean matches(String pattern, String path) {
        if (pattern.endsWith("/**")) {
            String prefix = pattern.substring(0, pattern.length() - 3);
            return path.equals(prefix) || path.startsWith(prefix + "/");
        }
        return path.equals(pattern);
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey signingKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 64) {
            StringBuilder sb = new StringBuilder(jwtProperties.getSecret());
            while (sb.length() < 64) {
                sb.append(jwtProperties.getSecret());
            }
            keyBytes = sb.toString().substring(0, 64).getBytes(StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String roles(Object value) {
        if (value instanceof List<?> list) {
            StringJoiner joiner = new StringJoiner(",");
            for (Object item : list) {
                joiner.add(String.valueOf(item));
            }
            return joiner.toString();
        }
        return value == null ? "" : String.valueOf(value);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, ErrorCode code) {
        return writeError(exchange, HttpStatus.UNAUTHORIZED, code);
    }

    private Mono<Void> writeError(
            ServerWebExchange exchange,
            HttpStatus status,
            ErrorCode code
    ) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        Result<Void> body = Result.error(code.getCode(), code.getMessage());
        DataBuffer buffer = response.bufferFactory().wrap(writeJson(body));
        return response.writeWith(Mono.just(buffer));
    }

    private byte[] writeJson(Result<Void> body) {
        try {
            return objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            return "{\"code\":20100,\"message\":\"未登录\"}".getBytes(StandardCharsets.UTF_8);
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }
}

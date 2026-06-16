package com.mallcloud.mallgateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mallcloud.mallcommon.response.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Gateway 内部路径阻断 + 内部 header 净化过滤器。
 *
 * 三重职责：
 *  1) 阻断 /api/v1/users/internal/** 外部访问（直接 404 + 资源不存在 body）；
 *  2) 阻断任何 /api/v1/&lt;业务段&gt;/internal/** 外部访问（防御纵深，避免未来新增
 *     /api/v1/products/internal/**、/api/v1/orders/internal/** 等接口时漏审；
 *     2026-06-13 Sprint 3.7 全域审计后由单一路径升级为通用路径模式）；
 *  3) 净化所有外部请求的 X-Internal-* header，防止客户端伪造内部 token 借 Feign 链路访问内部接口。
 *
 * 设计目标：internal endpoint 只能被服务间直接调用（Feign + LoadBalancer 走 mall-user 服务名），
 * 客户端从 Gateway 入口的访问一律返回 404，避免普通用户经 token 访问 internal endpoint 读取数据。
 *
 * 当前 Gateway 路由表实际暴露的 internal 路径仅 mall-user::UserController 的
 * /api/v1/users/internal/{userId} 和 /api/v1/users/internal/{userId}/addresses/{addressId}；
 * mall-order / mall-product / mall-inventory / mall-search / mall-seckill 的
 * /internal/** 端点仅以裸路径形式存在（如 /internal/orders/...），Gateway 路由表未配置对应
 * 入口，因此当前无 Gateway 暴露面。通用正则属于防御纵深，不是修复当前漏洞。
 *
 * 与 mall-user 内部的 X-Internal-Token 校验（CommonConstants.HEADER_INTERNAL_TOKEN）构成三层防护：
 *  - 本过滤器外层：阻断 internal 路径 + 删除外部 X-Internal-* header
 *  - mall-user 内层：assertInternalToken 校验（配置 mall.internal.token）
 *  - FeignUserInterceptor：禁止从入站请求透传 X-Internal-*
 *  FeignInternalTokenInterceptor 是 X-Internal-Token 注入的唯一来源（从配置读取）。
 *
 * 顺序：Ordered.HIGHEST_PRECEDENCE + 10，在 JwtAuthFilter 之前执行，
 * 避免给未授权的 internal 请求打上 userId 头再放行到 mall-user。
 */
@Slf4j
@Component
public class InternalPathBlockFilter implements GlobalFilter, Ordered {

    /**
     * 通用内部路径正则：匹配 /api/v1/{业务段}/internal 或 /api/v1/{业务段}/internal/**。
     * 第一段业务段仅允许字母/数字/横线，避免误伤 /api/v1/users/me/addresses 这类业务路径。
     */
    private static final Pattern INTERNAL_PATH =
            Pattern.compile("^/api/v1/[A-Za-z0-9_-]+/internal(/.*)?$");

    /**
     * 特定服务的内部路径正则：保留作为显式语义锚点（mall-user 历史上第一个接入 internal 保护）。
     * 实际匹配已被上方通用正则覆盖；保留此常量便于阅读与回归测试。
     */
    private static final Pattern USERS_INTERNAL_PATH =
            Pattern.compile("^/api/v1/users/internal(/.*)?$");

    private static final String INTERNAL_HEADER_PREFIX = "X-Internal-";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1) 内部路径直接阻断
        if (INTERNAL_PATH.matcher(path).matches()) {
            log.warn("[InternalPathBlocked] method={} path={} remote={}",
                    request.getMethod(), path, request.getRemoteAddress());
            return writeBlocked(exchange);
        }

        // 2) 非内部路径：净化 X-Internal-* header，防止客户端伪造
        ServerHttpRequest sanitized = sanitizeInternalHeaders(request);
        if (sanitized != request) {
            log.debug("[InternalHeaderStripped] method={} path={} remote={}",
                    request.getMethod(), path, request.getRemoteAddress());
            return chain.filter(exchange.mutate().request(sanitized).build());
        }
        return chain.filter(exchange);
    }

    /**
     * 删除入站请求中所有 X-Internal-* header，返回新请求对象。
     * 如无 X-Internal-* header，返回原对象（避免不必要的 mutate 分配）。
     */
    private ServerHttpRequest sanitizeInternalHeaders(ServerHttpRequest request) {
        List<String> toRemove = new ArrayList<>();
        request.getHeaders().forEach((name, values) -> {
            if (name != null && name.startsWith(INTERNAL_HEADER_PREFIX)) {
                toRemove.add(name);
            }
        });
        if (toRemove.isEmpty()) {
            return request;
        }
        ServerHttpRequest.Builder builder = request.mutate();
        for (String name : toRemove) {
            builder.headers(h -> h.remove(name));
        }
        return builder.build();
    }

    private Mono<Void> writeBlocked(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.NOT_FOUND);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        // 与 mall-common 的 NoResourceFoundException 处理器返回风格保持一致
        Result<Void> body = Result.error(404, "资源不存在");
        DataBuffer buffer;
        try {
            buffer = response.bufferFactory().wrap(objectMapper.writeValueAsBytes(body));
        } catch (JsonProcessingException e) {
            buffer = response.bufferFactory().wrap(
                    "{\"code\":404,\"message\":\"资源不存在\"}".getBytes(StandardCharsets.UTF_8));
        }
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        // 早于 JwtAuthFilter (-100)，避免给未授权请求打 userId 头再放行
        return -200;
    }

    /**
     * 暴露 INTERNAL_PATH 给集成测试用，避免硬编码正则。
     */
    public static Pattern getInternalPathPattern() {
        return INTERNAL_PATH;
    }

    /**
     * 暴露 USERS_INTERNAL_PATH 给集成测试用。
     */
    public static Pattern getUsersInternalPathPattern() {
        return USERS_INTERNAL_PATH;
    }
}

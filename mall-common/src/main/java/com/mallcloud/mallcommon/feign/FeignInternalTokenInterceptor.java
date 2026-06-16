package com.mallcloud.mallcommon.feign;

import com.mallcloud.mallcommon.config.InternalAuthProperties;
import com.mallcloud.mallcommon.constant.CommonConstants;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

/**
 * Feign 拦截器：服务间调用时自动注入 X-Internal-Token header。
 *
 * 仅当 X-Internal-Token 配置非空且非默认值占位时注入；配置缺失时不注入（被调方会按缺失拒绝）。
 * 路径限制：本拦截器作用于所有 Feign 调用；如需限定 internal endpoint，
 * 建议在调用方 Feign 接口上声明专用 contextId，并配合被调方 controller 的 header 校验。
 *
 * 配合 mall-gateway 的 InternalPathBlockFilter 实现双层防护：
 *  1. Gateway 阻断外部 /api/v1/users/internal/** 直接访问（404）；
 *  2. mall-user 等被调方要求 X-Internal-Token 校验通过（否则 401）。
 *
 * 注意：本拦截器不会从客户端请求上下文复制任何内部 header；它只读自身配置。
 */
@RequiredArgsConstructor
public class FeignInternalTokenInterceptor implements RequestInterceptor {

    private final InternalAuthProperties properties;

    @Override
    public void apply(RequestTemplate template) {
        if (properties == null) {
            return;
        }
        String token = properties.getToken();
        if (!StringUtils.hasText(token)) {
            return;
        }
        // 避免重复覆盖：仅当调用方未显式传入时才注入
        if (template.headers().get(CommonConstants.HEADER_INTERNAL_TOKEN) == null) {
            template.header(CommonConstants.HEADER_INTERNAL_TOKEN, token);
        }
    }
}

package com.mallcloud.mallcommon.feign;

import com.mallcloud.mallcommon.constant.CommonConstants;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign 拦截器：透传用户上下文 + 链路 ID
 *
 * @author zhangsan
 * @since 2026-03-01
 */
public class FeignUserInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return;
        HttpServletRequest request = attrs.getRequest();
        // 透传用户 ID
        String uid = request.getHeader(CommonConstants.HEADER_USER_ID);
        if (uid != null) template.header(CommonConstants.HEADER_USER_ID, uid);
        // 透传角色
        String roles = request.getHeader(CommonConstants.HEADER_USER_ROLES);
        if (roles != null) template.header(CommonConstants.HEADER_USER_ROLES, roles);
        // 透传 traceId
        String trace = request.getHeader(CommonConstants.HEADER_TRACE_ID);
        if (trace != null) template.header(CommonConstants.HEADER_TRACE_ID, trace);
        // 严禁透传 X-Internal-* header：内部鉴权由 FeignInternalTokenInterceptor 注入，
        // 防止客户端伪造 X-Internal-Token 借 Feign 链路访问内部接口。
        // （Gateway 侧 InternalPathBlockFilter 也会删除外部请求中的 X-Internal-*，双层防护。）
    }
}

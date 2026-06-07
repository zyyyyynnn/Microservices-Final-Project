package com.mallcloud.mallseckill.config;

import com.mallcloud.mallcommon.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 秒杀服务用户上下文拦截器
 *
 * @author wangwu
 * @since 2026-03-01
 */
@Component
public class SeckillUserContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Long userId = UserContext.parseUserIdFromHeader(request);
        if (userId != null) {
            UserContext.setUserId(userId);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}

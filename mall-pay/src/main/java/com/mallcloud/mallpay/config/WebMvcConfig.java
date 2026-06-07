package com.mallcloud.mallpay.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 支付服务 Web 配置
 *
 * @author wangwu
 * @since 2026-03-01
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final PayUserContextInterceptor payUserContextInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(payUserContextInterceptor)
                .addPathPatterns("/api/v1/pay/**")
                .excludePathPatterns("/api/v1/pay/notify", "/api/v1/pay/ping");
    }
}

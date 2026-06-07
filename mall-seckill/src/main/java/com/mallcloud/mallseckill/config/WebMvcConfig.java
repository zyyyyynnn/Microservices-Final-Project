package com.mallcloud.mallseckill.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 *
 * @author wangwu
 * @since 2026-03-01
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final SeckillUserContextInterceptor seckillUserContextInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(seckillUserContextInterceptor)
                .addPathPatterns("/api/v1/seckill/**")
                .excludePathPatterns("/api/v1/seckill/ping", "/api/v1/seckill/activities/**");
    }
}

package com.mallcloud.mallcommon;

import com.mallcloud.mallcommon.config.InternalAuthProperties;
import com.mallcloud.mallcommon.feign.FeignInternalTokenInterceptor;
import com.mallcloud.mallcommon.feign.FeignUserInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * mall-common 自动装配入口
 */
@Configuration
@ComponentScan(basePackages = "com.mallcloud.mallcommon")
@AutoConfiguration
@EnableConfigurationProperties(InternalAuthProperties.class)
public class MallCommonAutoConfiguration {

    @Bean
    public FeignUserInterceptor feignUserInterceptor() {
        return new FeignUserInterceptor();
    }

    /**
     * 服务间鉴权 Feign 拦截器：从 mall.internal.token 配置注入 X-Internal-Token header。
     * 任何引用 mall-common 的微服务都自动获得该拦截器（Spring Boot 自动装配）。
     */
    @Bean
    public FeignInternalTokenInterceptor feignInternalTokenInterceptor(InternalAuthProperties properties) {
        return new FeignInternalTokenInterceptor(properties);
    }
}

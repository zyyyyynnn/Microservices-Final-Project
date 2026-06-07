package com.mallcloud.mallcommon;

import com.mallcloud.mallcommon.feign.FeignUserInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * mall-common 自动装配入口
 *
 * @author zhangsan
 * @since 2026-03-01
 */
@Configuration
@ComponentScan(basePackages = "com.mallcloud.mallcommon")
@AutoConfiguration
public class MallCommonAutoConfiguration {

    @Bean
    public FeignUserInterceptor feignUserInterceptor() {
        return new FeignUserInterceptor();
    }
}

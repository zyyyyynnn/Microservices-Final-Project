package com.mallcloud.mallgateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * 网关 JWT 配置
 *
 * @author zhangsan
 * @since 2026-03-01
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "mallcloud.jwt")
public class GatewayJwtProperties {

    private String secret;
    private String header = "Authorization";
    private String prefix = "Bearer ";
    private List<String> whitelist = new ArrayList<>();
}

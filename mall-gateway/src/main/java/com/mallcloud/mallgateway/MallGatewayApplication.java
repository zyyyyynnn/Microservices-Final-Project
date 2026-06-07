package com.mallcloud.mallgateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API 网关启动类
 *
 * 职责：路由转发 + JWT 鉴权 + 全局限流 + 跨域
 *
 * @author zhangsan
 * @since 2026-03-01
 */
@Slf4j
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"com.mallcloud.mallgateway", "com.mallcloud.mallcommon"})
public class MallGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallGatewayApplication.class, args);
        log.info("MallGateway started on port 9000");
    }
}

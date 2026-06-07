package com.mallcloud.mallauth;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 认证服务启动类
 *
 * 职责：用户登录、Token 颁发、Token 刷新、验证码
 *
 * @author zhangsan
 * @since 2026-03-01
 */
@Slf4j
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.mallcloud.mallauth.client")
@EnableTransactionManagement
@MapperScan("com.mallcloud.mallauth.mapper")
@SpringBootApplication(scanBasePackages = {"com.mallcloud.mallauth", "com.mallcloud.mallcommon"})
public class MallAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallAuthApplication.class, args);
        log.info("MallAuth started on port 9001");
    }
}

package com.mallcloud.malluser;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 用户服务启动类
 *
 * 职责：用户资料、收货地址、注册信息
 *
 * @author zhangsan
 * @since 2026-03-01
 */
@Slf4j
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.mallcloud.malluser.client")
@EnableTransactionManagement
@MapperScan("com.mallcloud.malluser.mapper")
@SpringBootApplication(scanBasePackages = {"com.mallcloud.malluser", "com.mallcloud.mallcommon"})
public class MallUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallUserApplication.class, args);
        log.info("MallUser started on port 9002");
    }
}

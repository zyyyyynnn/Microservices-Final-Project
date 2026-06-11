package com.mallcloud.mallcart;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 购物车服务启动类
 *
 * 职责：购物车 CRUD（Redis Hash 存储）
 *
 * @author wangwu
 * @since 2026-03-01
 */
@Slf4j
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.mallcloud.mallcart.client")
@SpringBootApplication(scanBasePackages = {"com.mallcloud.mallcart", "com.mallcloud.mallcommon"})
public class MallCartApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallCartApplication.class, args);
        log.info("MallCart started on port 9105");
    }
}

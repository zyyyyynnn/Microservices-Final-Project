package com.mallcloud.mallmessage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 消息服务启动类
 *
 * 职责：MQ 生产/消费封装、订单消息路由、库存回滚消费者
 *
 * @author zhaoliu
 * @since 2026-03-01
 */
@Slf4j
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.mallcloud.mallmessage.client")
@SpringBootApplication(scanBasePackages = {"com.mallcloud.mallmessage", "com.mallcloud.mallcommon"})
public class MallMessageApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallMessageApplication.class, args);
        log.info("MallMessage started on port 9010");
    }
}

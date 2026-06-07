package com.mallcloud.malljob;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 定时任务服务启动类
 *
 * 职责：订单超时关闭、库存对账、ES 全量同步
 *
 * @author zhaoliu
 * @since 2026-03-01
 */
@Slf4j
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.mallcloud.malljob.client")
@EnableScheduling
@SpringBootApplication(scanBasePackages = {"com.mallcloud.malljob", "com.mallcloud.mallcommon"})
public class MallJobApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallJobApplication.class, args);
        log.info("MallJob started on port 9012");
    }
}

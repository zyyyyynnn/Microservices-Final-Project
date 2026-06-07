package com.mallcloud.mallsearch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 搜索服务启动类
 *
 * 职责：ES 全文检索、热词、聚合
 *
 * @author lisi
 * @since 2026-03-01
 */
@Slf4j
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.mallcloud.mallsearch.client")
@SpringBootApplication(scanBasePackages = {"com.mallcloud.mallsearch", "com.mallcloud.mallcommon"})
public class MallSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallSearchApplication.class, args);
        log.info("MallSearch started on port 9008");
    }
}

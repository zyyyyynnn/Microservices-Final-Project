package com.mallcloud.malladminbiz;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 后台业务服务启动类
 *
 * 职责：商家后台聚合（看板、订单管理、商品管理）
 *
 * @author zhaoliu
 * @since 2026-03-01
 */
@Slf4j
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.mallcloud.malladminbiz.client")
@SpringBootApplication(scanBasePackages = {"com.mallcloud.malladminbiz", "com.mallcloud.mallcommon"})
public class MallAdminBizApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallAdminBizApplication.class, args);
        log.info("MallAdminBiz started on port 9111");
    }
}

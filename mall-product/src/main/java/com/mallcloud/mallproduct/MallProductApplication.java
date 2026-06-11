package com.mallcloud.mallproduct;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 商品服务启动类
 *
 * 职责：类目、SPU、SKU、上下架、商品属性
 *
 * @author lisi
 * @since 2026-03-01
 */
@Slf4j
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.mallcloud.mallproduct.client")
@EnableTransactionManagement
@MapperScan("com.mallcloud.mallproduct.mapper")
@SpringBootApplication(scanBasePackages = {"com.mallcloud.mallproduct", "com.mallcloud.mallcommon"})
public class MallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallProductApplication.class, args);
        log.info("MallProduct started on port 9103");
    }
}

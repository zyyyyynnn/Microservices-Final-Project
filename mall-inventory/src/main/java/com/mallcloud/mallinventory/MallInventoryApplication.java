package com.mallcloud.mallinventory;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 库存服务启动类
 *
 * 职责：库存预扣、扣减、回滚、对账
 *
 * @author wangwu
 * @since 2026-03-01
 */
@Slf4j
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.mallcloud.mallinventory.client")
@EnableTransactionManagement
@MapperScan("com.mallcloud.mallinventory.mapper")
@SpringBootApplication(scanBasePackages = {"com.mallcloud.mallinventory", "com.mallcloud.mallcommon"})
public class MallInventoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallInventoryApplication.class, args);
        log.info("MallInventory started on port 9104");
    }
}

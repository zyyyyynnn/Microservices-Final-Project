package com.mallcloud.mallorder;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 订单服务启动类
 *
 * @author wangwu
 * @since 2026-03-01
 */
@Slf4j
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.mallcloud.mallorder.client")
@EnableTransactionManagement
@MapperScan("com.mallcloud.mallorder.mapper")
@SpringBootApplication(scanBasePackages = {"com.mallcloud.mallorder", "com.mallcloud.mallcommon"})
public class MallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallOrderApplication.class, args);
        log.info("MallOrder started on port 9006");
    }
}

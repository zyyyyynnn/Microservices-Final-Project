package com.mallcloud.mallpay;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 支付服务启动类
 *
 * 职责：沙箱支付（支付宝/微信）、退款、MQ 通知
 *
 * @author wangwu
 * @since 2026-03-01
 */
@Slf4j
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.mallcloud.mallpay.client")
@EnableTransactionManagement
@MapperScan("com.mallcloud.mallpay.mapper")
@SpringBootApplication(scanBasePackages = {"com.mallcloud.mallpay", "com.mallcloud.mallcommon"})
public class MallPayApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallPayApplication.class, args);
        log.info("MallPay started on port 9007");
    }
}

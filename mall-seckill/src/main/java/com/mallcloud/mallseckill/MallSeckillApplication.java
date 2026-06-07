package com.mallcloud.mallseckill;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 秒杀服务启动类
 *
 * 职责：Redis Lua 预扣、Sentinel 限流、RocketMQ 异步下单
 *
 * @author wangwu
 * @since 2026-03-01
 */
@Slf4j
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.mallcloud.mallseckill.client")
@EnableTransactionManagement
@MapperScan("com.mallcloud.mallseckill.mapper")
@SpringBootApplication(scanBasePackages = {"com.mallcloud.mallseckill", "com.mallcloud.mallcommon"})
public class MallSeckillApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallSeckillApplication.class, args);
        log.info("MallSeckill started on port 9009");
    }
}

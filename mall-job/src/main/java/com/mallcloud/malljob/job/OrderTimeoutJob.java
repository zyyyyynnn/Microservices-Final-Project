package com.mallcloud.malljob.job;

import com.mallcloud.malljob.service.JobTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 订单超时关闭任务
 *
 * @author zhaoliu
 * @since 2026-03-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutJob {

    private final JobTaskService jobTaskService;

    /**
     * 每 5 分钟扫描一次超时未支付订单
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void scanTimeoutOrders() {
        int count = jobTaskService.closeTimeoutOrders();
        log.info("[Job] 订单超时关闭完成 count={}", count);
    }
}

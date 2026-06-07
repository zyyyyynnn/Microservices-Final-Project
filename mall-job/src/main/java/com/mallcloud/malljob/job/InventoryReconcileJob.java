package com.mallcloud.malljob.job;

import com.mallcloud.malljob.service.JobTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 库存对账任务
 *
 * @author zhaoliu
 * @since 2026-03-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryReconcileJob {

    private final JobTaskService jobTaskService;

    /**
     * 每 10 分钟执行一次库存对账
     */
    @Scheduled(cron = "0 */10 * * * ?")
    public void reconcileInventory() {
        int count = jobTaskService.reconcileInventory();
        log.info("[Job] 库存对账完成 count={}", count);
    }
}

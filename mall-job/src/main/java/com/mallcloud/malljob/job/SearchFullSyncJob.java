package com.mallcloud.malljob.job;

import com.mallcloud.malljob.service.JobTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 商品搜索全量同步任务
 *
 * @author zhaoliu
 * @since 2026-03-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchFullSyncJob {

    private final JobTaskService jobTaskService;

    /**
     * 每天凌晨 2 点同步上架商品到 ES
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void syncAllProducts() {
        int count = jobTaskService.syncAllProductsToSearch();
        log.info("[Job] 商品搜索全量同步完成 count={}", count);
    }
}

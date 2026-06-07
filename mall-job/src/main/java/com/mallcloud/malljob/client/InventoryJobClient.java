package com.mallcloud.malljob.client;

import com.mallcloud.mallcommon.response.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 库存任务 Feign
 *
 * @author zhaoliu
 * @since 2026-03-01
 */
@FeignClient(name = "mall-inventory")
public interface InventoryJobClient {

    @PostMapping("/internal/jobs/inventory/reconcile")
    Result<Integer> reconcileStock();
}

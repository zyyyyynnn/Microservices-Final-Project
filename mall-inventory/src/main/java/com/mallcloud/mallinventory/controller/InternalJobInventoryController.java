package com.mallcloud.mallinventory.controller;

import com.mallcloud.mallcommon.response.Result;
import com.mallcloud.mallinventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 库存任务内部接口
 *
 * @author zhaoliu
 * @since 2026-03-01
 */
@RestController
@RequestMapping("/internal/jobs/inventory")
@RequiredArgsConstructor
public class InternalJobInventoryController {

    private final InventoryService inventoryService;

    /**
     * 库存对账
     */
    @PostMapping("/reconcile")
    public Result<Integer> reconcileStock() {
        return Result.ok(inventoryService.reconcileStock());
    }
}

package com.mallcloud.mallinventory.controller;

import com.mallcloud.mallcommon.response.Result;
import com.mallcloud.mallinventory.api.dto.LockStockDTO;
import com.mallcloud.mallinventory.api.dto.OrderNoDTO;
import com.mallcloud.mallinventory.api.vo.StockVO;
import com.mallcloud.mallinventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "库存接口")
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @Operation(summary = "健康检查")
    @GetMapping("/ping")
    public Result<String> ping() {
        return Result.ok("mall-inventory pong");
    }

    @Operation(summary = "锁定库存")
    @PostMapping("/lock")
    public Result<Void> lockStock(@RequestBody LockStockDTO dto) {
        inventoryService.lock(dto);
        return Result.ok();
    }

    @Operation(summary = "扣减库存")
    @PostMapping("/deduct")
    public Result<Void> deductStock(@RequestBody OrderNoDTO dto) {
        inventoryService.deduct(dto);
        return Result.ok();
    }

    @Operation(summary = "释放库存")
    @PostMapping("/release")
    public Result<Void> releaseStock(@RequestBody OrderNoDTO dto) {
        inventoryService.release(dto);
        return Result.ok();
    }

    @Operation(summary = "查询库存")
    @GetMapping("/stock/{skuId}")
    public Result<StockVO> getStock(@PathVariable("skuId") Long skuId) {
        return Result.ok(inventoryService.getStock(skuId));
    }
}

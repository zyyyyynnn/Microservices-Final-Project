package com.mallcloud.mallproduct.controller;

import com.mallcloud.mallcommon.response.Result;
import com.mallcloud.mallproduct.service.SpuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商品任务内部接口
 *
 * @author lisi
 * @since 2026-03-01
 */
@RestController
@RequestMapping("/internal/jobs/products")
@RequiredArgsConstructor
public class InternalJobProductController {

    private final SpuService spuService;

    /**
     * 查询上架 SPU ID 列表
     */
    @GetMapping("/on-sale-spu-ids")
    public Result<List<Long>> listOnSaleSpuIds() {
        return Result.ok(spuService.listOnSaleSpuIds());
    }
}

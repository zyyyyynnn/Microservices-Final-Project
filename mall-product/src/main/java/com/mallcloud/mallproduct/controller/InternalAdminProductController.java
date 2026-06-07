package com.mallcloud.mallproduct.controller;

import com.mallcloud.mallcommon.response.PageData;
import com.mallcloud.mallcommon.response.Result;
import com.mallcloud.mallproduct.api.dto.AdminProductQueryDTO;
import com.mallcloud.mallproduct.api.vo.AdminProductVO;
import com.mallcloud.mallproduct.api.vo.ProductStatsVO;
import com.mallcloud.mallproduct.service.SpuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 后台商品内部接口
 *
 * @author lisi
 * @since 2026-03-01
 */
@RestController
@RequestMapping("/internal/admin/products")
@RequiredArgsConstructor
public class InternalAdminProductController {

    private final SpuService spuService;

    /**
     * 查询商品统计
     */
    @GetMapping("/stats")
    public Result<ProductStatsVO> getProductStats() {
        return Result.ok(spuService.getProductStats());
    }

    /**
     * 查询后台商品列表
     */
    @GetMapping
    public Result<PageData<AdminProductVO>> listProducts(AdminProductQueryDTO query) {
        return Result.ok(spuService.listAdminProducts(query));
    }
}

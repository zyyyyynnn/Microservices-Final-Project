package com.mallcloud.malladminbiz.controller;

import com.mallcloud.malladminbiz.api.dto.AdminOrderQueryDTO;
import com.mallcloud.malladminbiz.api.dto.AdminProductQueryDTO;
import com.mallcloud.malladminbiz.api.dto.ShipOrderDTO;
import com.mallcloud.malladminbiz.api.vo.AdminOrderVO;
import com.mallcloud.malladminbiz.api.vo.AdminProductVO;
import com.mallcloud.malladminbiz.api.vo.DashboardVO;
import com.mallcloud.malladminbiz.service.AdminDashboardService;
import com.mallcloud.malladminbiz.service.AdminOrderService;
import com.mallcloud.malladminbiz.service.AdminProductService;
import com.mallcloud.mallcommon.response.PageData;
import com.mallcloud.mallcommon.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 后台业务接口
 *
 * @author zhaoliu
 * @since 2026-03-01
 */
@Tag(name = "后台业务")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminDashboardService adminDashboardService;
    private final AdminOrderService adminOrderService;
    private final AdminProductService adminProductService;

    /**
     * 健康检查
     */
    @Operation(summary = "健康检查")
    @GetMapping("/ping")
    public Result<String> ping() {
        return Result.ok("mall-admin-biz pong");
    }

    /**
     * 查询后台数据看板
     */
    @Operation(summary = "数据看板")
    @GetMapping("/dashboard")
    public Result<DashboardVO> dashboard() {
        return Result.ok(adminDashboardService.getDashboard());
    }

    /**
     * 查询商家订单列表
     */
    @Operation(summary = "商家订单列表")
    @GetMapping("/orders")
    public Result<PageData<AdminOrderVO>> listOrders(AdminOrderQueryDTO query) {
        return Result.ok(adminOrderService.listOrders(query));
    }

    /**
     * 商家发货
     */
    @Operation(summary = "商家发货")
    @PostMapping("/orders/{orderNo}/ship")
    public Result<Void> shipOrder(@PathVariable("orderNo") String orderNo, @RequestBody ShipOrderDTO dto) {
        adminOrderService.shipOrder(orderNo, dto);
        return Result.ok();
    }

    /**
     * 查询商家商品列表
     */
    @Operation(summary = "商家商品列表")
    @GetMapping("/products")
    public Result<PageData<AdminProductVO>> listProducts(AdminProductQueryDTO query) {
        return Result.ok(adminProductService.listProducts(query));
    }
}

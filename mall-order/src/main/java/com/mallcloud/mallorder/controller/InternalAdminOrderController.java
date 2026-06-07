package com.mallcloud.mallorder.controller;

import com.mallcloud.mallcommon.response.PageData;
import com.mallcloud.mallcommon.response.Result;
import com.mallcloud.mallorder.api.dto.AdminOrderQueryDTO;
import com.mallcloud.mallorder.api.dto.ShipOrderDTO;
import com.mallcloud.mallorder.api.vo.AdminOrderVO;
import com.mallcloud.mallorder.api.vo.OrderStatsVO;
import com.mallcloud.mallorder.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 后台订单内部接口
 *
 * @author wangwu
 * @since 2026-03-01
 */
@RestController
@RequestMapping("/internal/admin/orders")
@RequiredArgsConstructor
public class InternalAdminOrderController {

    private final OrderService orderService;

    /**
     * 查询订单统计
     */
    @GetMapping("/stats")
    public Result<OrderStatsVO> getOrderStats() {
        return Result.ok(orderService.getOrderStats());
    }

    /**
     * 查询后台订单列表
     */
    @GetMapping
    public Result<PageData<AdminOrderVO>> listOrders(AdminOrderQueryDTO query) {
        return Result.ok(orderService.listAdminOrders(query));
    }

    /**
     * 订单发货
     */
    @PostMapping("/{orderNo}/ship")
    public Result<Void> shipOrder(@PathVariable("orderNo") String orderNo, @RequestBody ShipOrderDTO dto) {
        orderService.shipOrder(orderNo, dto);
        return Result.ok();
    }
}

package com.mallcloud.mallorder.controller;

import com.mallcloud.mallcommon.response.Result;
import com.mallcloud.mallorder.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单任务内部接口
 *
 * @author wangwu
 * @since 2026-03-01
 */
@RestController
@RequestMapping("/internal/jobs/orders")
@RequiredArgsConstructor
public class InternalJobOrderController {

    private final OrderService orderService;

    /**
     * 关闭超时未支付订单
     */
    @PostMapping("/timeout/close")
    public Result<Integer> closeTimeoutOrders() {
        return Result.ok(orderService.closeTimeoutOrders());
    }
}

package com.mallcloud.mallorder.controller;

import com.mallcloud.mallcommon.response.Result;
import com.mallcloud.mallorder.api.dto.SeckillOrderCreateDTO;
import com.mallcloud.mallorder.api.vo.SeckillOrderVO;
import com.mallcloud.mallorder.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 订单内部接口
 *
 * @author wangwu
 * @since 2026-03-01
 */
@RestController
@RequestMapping("/internal/orders")
@RequiredArgsConstructor
public class InternalOrderController {

    private final OrderService orderService;

    /**
     * 标记订单已支付
     */
    @PostMapping("/{orderNo}/paid")
    public Result<Void> markPaid(@PathVariable("orderNo") String orderNo) {
        orderService.markPaid(orderNo);
        return Result.ok();
    }

    /**
     * 创建秒杀订单
     */
    @PostMapping("/seckill")
    public Result<SeckillOrderVO> createSeckillOrder(@RequestBody SeckillOrderCreateDTO dto) {
        return Result.ok(orderService.createSeckillOrder(dto));
    }
}

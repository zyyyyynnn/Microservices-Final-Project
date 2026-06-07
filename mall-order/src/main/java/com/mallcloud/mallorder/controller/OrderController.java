package com.mallcloud.mallorder.controller;

import com.mallcloud.mallcommon.exception.BizException;
import com.mallcloud.mallcommon.response.Result;
import com.mallcloud.mallcommon.util.UserContext;
import com.mallcloud.mallorder.api.dto.CreateOrderDTO;
import com.mallcloud.mallorder.api.vo.CreateOrderVO;
import com.mallcloud.mallorder.api.vo.OrderVO;
import com.mallcloud.mallorder.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Result<CreateOrderVO> create(@Valid @RequestBody CreateOrderDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) throw new BizException(20100, "未登录");
        return Result.ok(orderService.createOrder(userId, dto));
    }

    @GetMapping("/{orderNo}")
    public Result<OrderVO> getOrder(@PathVariable("orderNo") String orderNo) {
        Long userId = UserContext.getUserId();
        if (userId == null) throw new BizException(20100, "未登录");
        return Result.ok(orderService.getOrder(orderNo, userId));
    }
}

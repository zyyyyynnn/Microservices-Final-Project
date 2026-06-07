package com.mallcloud.mallseckill.controller;

import com.mallcloud.mallcommon.response.Result;
import com.mallcloud.mallseckill.service.SeckillService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 秒杀内部接口
 *
 * @author wangwu
 * @since 2026-03-01
 */
@RestController
@RequestMapping("/internal/seckill")
@RequiredArgsConstructor
public class InternalSeckillController {

    private final SeckillService seckillService;

    /**
     * 标记秒杀请求成功
     */
    @PostMapping("/result/{requestId}/success")
    public Result<Void> markSuccess(@PathVariable("requestId") String requestId,
                                    @RequestParam("orderNo") String orderNo) {
        seckillService.markSuccess(requestId, orderNo);
        return Result.ok();
    }

    /**
     * 标记秒杀请求失败
     */
    @PostMapping("/result/{requestId}/fail")
    public Result<Void> markFailed(@PathVariable("requestId") String requestId,
                                   @RequestParam("reason") String reason) {
        seckillService.markFailed(requestId, reason);
        return Result.ok();
    }
}

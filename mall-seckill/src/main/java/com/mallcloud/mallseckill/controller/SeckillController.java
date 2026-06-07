package com.mallcloud.mallseckill.controller;

import com.mallcloud.mallcommon.response.Result;
import com.mallcloud.mallseckill.api.dto.SeckillCreateDTO;
import com.mallcloud.mallseckill.api.vo.SeckillActivityVO;
import com.mallcloud.mallseckill.api.vo.SeckillCreateVO;
import com.mallcloud.mallseckill.api.vo.SeckillResultVO;
import com.mallcloud.mallseckill.service.SeckillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 秒杀服务接口
 *
 * @author wangwu
 * @since 2026-03-01
 */
@Tag(name = "秒杀接口")
@RestController
@RequestMapping("/api/v1/seckill")
@RequiredArgsConstructor
public class SeckillController {

    private final SeckillService seckillService;

    /**
     * 健康检查
     */
    @Operation(summary = "健康检查")
    @GetMapping("/ping")
    public Result<String> ping() {
        return Result.ok("mall-seckill pong");
    }

    /**
     * 查询秒杀活动列表
     */
    @Operation(summary = "秒杀活动列表")
    @GetMapping("/activities")
    public Result<List<SeckillActivityVO>> listActivities() {
        return Result.ok(seckillService.listActivities());
    }

    /**
     * 查询秒杀活动详情
     */
    @Operation(summary = "秒杀活动详情")
    @GetMapping("/activities/{id}")
    public Result<SeckillActivityVO> getActivity(@PathVariable("id") Long id) {
        return Result.ok(seckillService.getActivity(id));
    }

    /**
     * 发起秒杀
     */
    @Operation(summary = "秒杀下单")
    @PostMapping("/{activityId}")
    public Result<SeckillCreateVO> create(@PathVariable("activityId") Long activityId,
                                          @Valid @RequestBody SeckillCreateDTO dto) {
        return Result.ok(seckillService.create(activityId, dto));
    }

    /**
     * 查询秒杀结果
     */
    @Operation(summary = "轮询秒杀结果")
    @GetMapping("/result/{requestId}")
    public Result<SeckillResultVO> getResult(@PathVariable("requestId") String requestId) {
        return Result.ok(seckillService.getResult(requestId));
    }
}

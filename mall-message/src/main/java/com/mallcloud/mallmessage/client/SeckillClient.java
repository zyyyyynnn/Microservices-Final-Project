package com.mallcloud.mallmessage.client;

import com.mallcloud.mallcommon.response.Result;
import com.mallcloud.mallmessage.client.vo.SeckillResultVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 秒杀服务 Feign
 *
 * @author zhaoliu
 * @since 2026-03-01
 */
@FeignClient(name = "mall-seckill")
public interface SeckillClient {

    @GetMapping("/internal/seckill/result/{requestId}")
    Result<SeckillResultVO> getResult(@PathVariable("requestId") String requestId);

    @PostMapping("/internal/seckill/result/{requestId}/success")
    Result<Void> markSuccess(@PathVariable("requestId") String requestId, @RequestParam("orderNo") String orderNo);

    @PostMapping("/internal/seckill/result/{requestId}/fail")
    Result<Void> markFailed(@PathVariable("requestId") String requestId, @RequestParam("reason") String reason);
}

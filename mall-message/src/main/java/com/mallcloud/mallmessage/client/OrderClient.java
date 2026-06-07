package com.mallcloud.mallmessage.client;

import com.mallcloud.mallcommon.response.Result;
import com.mallcloud.mallmessage.api.dto.SeckillOrderCreateDTO;
import com.mallcloud.mallmessage.client.vo.SeckillOrderVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 订单服务 Feign
 *
 * @author zhaoliu
 * @since 2026-03-01
 */
@FeignClient(name = "mall-order")
public interface OrderClient {

    @PostMapping("/internal/orders/{orderNo}/paid")
    Result<Void> markPaid(@PathVariable("orderNo") String orderNo);

    @PostMapping("/internal/orders/seckill")
    Result<SeckillOrderVO> createSeckillOrder(@RequestBody SeckillOrderCreateDTO dto);
}

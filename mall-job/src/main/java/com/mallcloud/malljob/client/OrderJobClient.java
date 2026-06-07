package com.mallcloud.malljob.client;

import com.mallcloud.mallcommon.response.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 订单任务 Feign
 *
 * @author zhaoliu
 * @since 2026-03-01
 */
@FeignClient(name = "mall-order")
public interface OrderJobClient {

    @PostMapping("/internal/jobs/orders/timeout/close")
    Result<Integer> closeTimeoutOrders();
}

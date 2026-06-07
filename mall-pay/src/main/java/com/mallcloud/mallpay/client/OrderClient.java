package com.mallcloud.mallpay.client;

import com.mallcloud.mallcommon.response.Result;
import com.mallcloud.mallpay.client.dto.OrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 订单服务远程调用
 *
 * @author wangwu
 * @since 2026-03-01
 */
@FeignClient(name = "mall-order")
public interface OrderClient {

    /**
     * 查询当前用户订单详情
     *
     * @param orderNo 订单号
     * @return 订单详情
     */
    @GetMapping("/api/v1/orders/{orderNo}")
    Result<OrderDTO> getOrder(@PathVariable("orderNo") String orderNo);
}

package com.mallcloud.malladminbiz.client;

import com.mallcloud.malladminbiz.api.dto.AdminOrderQueryDTO;
import com.mallcloud.malladminbiz.api.dto.ShipOrderDTO;
import com.mallcloud.malladminbiz.api.vo.AdminOrderVO;
import com.mallcloud.malladminbiz.client.dto.OrderStatsDTO;
import com.mallcloud.mallcommon.response.PageData;
import com.mallcloud.mallcommon.response.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;

/**
 * 订单后台 Feign
 *
 * @author zhaoliu
 * @since 2026-03-01
 */
@FeignClient(name = "mall-order")
public interface OrderAdminClient {

    @GetMapping("/internal/admin/orders/stats")
    Result<OrderStatsDTO> getOrderStats();

    @GetMapping("/internal/admin/orders")
    Result<PageData<AdminOrderVO>> listOrders(@SpringQueryMap AdminOrderQueryDTO query);

    @PostMapping("/internal/admin/orders/{orderNo}/ship")
    Result<Void> shipOrder(@PathVariable("orderNo") String orderNo, @RequestBody ShipOrderDTO dto);
}

package com.mallcloud.mallmessage.client;

import com.mallcloud.mallcommon.response.Result;
import com.mallcloud.mallmessage.client.dto.OrderNoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 库存服务 Feign
 *
 * @author zhaoliu
 * @since 2026-03-01
 */
@FeignClient(name = "mall-inventory")
public interface InventoryClient {

    @PostMapping("/api/v1/inventory/deduct")
    Result<Void> deduct(@RequestBody OrderNoDTO dto);

    @PostMapping("/api/v1/inventory/release")
    Result<Void> release(@RequestBody OrderNoDTO dto);
}

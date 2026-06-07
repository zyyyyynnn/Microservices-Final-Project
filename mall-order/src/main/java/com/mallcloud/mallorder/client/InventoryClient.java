package com.mallcloud.mallorder.client;

import com.mallcloud.mallorder.client.dto.LockDTO;
import com.mallcloud.mallcommon.response.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "mall-inventory", contextId = "inventoryClient")
public interface InventoryClient {
    @PostMapping("/api/v1/inventory/lock")
    Result<Void> lock(@RequestBody List<LockDTO> dtoList);
}

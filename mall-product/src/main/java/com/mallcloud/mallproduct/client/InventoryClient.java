package com.mallcloud.mallproduct.client;
import com.mallcloud.mallcommon.response.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "mall-inventory")
public interface InventoryClient {
    @GetMapping("/api/v1/inventory/stock/{skuId}")
    Result<Integer> getStock(@PathVariable("skuId") Long skuId);
}

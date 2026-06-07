package com.mallcloud.mallcart.client;

import com.mallcloud.mallcart.client.dto.SkuDTO;
import com.mallcloud.mallcommon.response.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "mall-product", contextId = "productClient")
public interface ProductClient {
    
    @GetMapping("/internal/products/skus/{skuId}")
    Result<SkuDTO> getSku(@PathVariable("skuId") Long skuId);
}

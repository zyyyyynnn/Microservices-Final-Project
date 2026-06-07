package com.mallcloud.mallsearch.client;

import com.mallcloud.mallcommon.response.Result;
import com.mallcloud.mallsearch.client.dto.ProductDetailDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 商品服务远程调用
 *
 * @author lisi
 * @since 2026-03-01
 */
@FeignClient(name = "mall-product")
public interface ProductClient {

    /**
     * 查询商品详情
     *
     * @param spuId SPU ID
     * @return 商品详情
     */
    @GetMapping("/api/v1/products/{id}")
    Result<ProductDetailDTO> getProduct(@PathVariable("id") Long spuId);
}

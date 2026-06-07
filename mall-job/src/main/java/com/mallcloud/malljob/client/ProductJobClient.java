package com.mallcloud.malljob.client;

import com.mallcloud.mallcommon.response.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * 商品任务 Feign
 *
 * @author zhaoliu
 * @since 2026-03-01
 */
@FeignClient(name = "mall-product")
public interface ProductJobClient {

    @GetMapping("/internal/jobs/products/on-sale-spu-ids")
    Result<List<Long>> listOnSaleSpuIds();
}

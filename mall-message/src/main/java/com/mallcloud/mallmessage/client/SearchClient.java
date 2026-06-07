package com.mallcloud.mallmessage.client;

import com.mallcloud.mallcommon.response.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 搜索服务 Feign
 *
 * @author zhaoliu
 * @since 2026-03-01
 */
@FeignClient(name = "mall-search")
public interface SearchClient {

    @PostMapping("/internal/search/products/{spuId}/sync")
    Result<Void> syncProduct(@PathVariable("spuId") Long spuId, @RequestParam("status") Integer status);
}

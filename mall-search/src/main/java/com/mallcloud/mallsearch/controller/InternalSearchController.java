package com.mallcloud.mallsearch.controller;

import com.mallcloud.mallcommon.response.Result;
import com.mallcloud.mallsearch.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 搜索内部接口
 *
 * @author lisi
 * @since 2026-03-01
 */
@RestController
@RequestMapping("/internal/search")
@RequiredArgsConstructor
public class InternalSearchController {

    private final SearchService searchService;

    /**
     * 同步商品搜索文档
     */
    @PostMapping("/products/{spuId}/sync")
    public Result<Void> syncProduct(@PathVariable("spuId") Long spuId,
                                    @RequestParam("status") Integer status) {
        searchService.syncProduct(spuId, status);
        return Result.ok();
    }
}

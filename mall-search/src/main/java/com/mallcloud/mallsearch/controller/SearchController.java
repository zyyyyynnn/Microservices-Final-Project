package com.mallcloud.mallsearch.controller;

import com.mallcloud.mallcommon.response.Result;
import com.mallcloud.mallsearch.api.dto.ProductSearchDTO;
import com.mallcloud.mallsearch.api.vo.ProductSearchVO;
import com.mallcloud.mallsearch.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 搜索服务接口（骨架）
 *
 * @author lisi
 * @since 2026-03-01
 */
@Tag(name = "搜索接口")
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @Operation(summary = "健康检查")
    @GetMapping("/ping")
    public Result<String> ping() {
        return Result.ok("mall-search pong");
    }

    @Operation(summary = "商品搜索")
    @GetMapping("/products")
    public Result<ProductSearchVO> searchProducts(@ModelAttribute ProductSearchDTO dto) {
        return Result.ok(searchService.searchProducts(dto));
    }

    @Operation(summary = "搜索热词")
    @GetMapping("/hot-words")
    public Result<List<String>> hotWords() {
        return Result.ok(searchService.getHotWords());
    }
}

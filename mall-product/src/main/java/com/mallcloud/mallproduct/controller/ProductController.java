package com.mallcloud.mallproduct.controller;

import com.mallcloud.mallcommon.response.Result;
import com.mallcloud.mallproduct.api.vo.CategoryTreeVO;
import com.mallcloud.mallproduct.api.vo.SpuDetailVO;
import com.mallcloud.mallproduct.service.CategoryService;
import com.mallcloud.mallproduct.service.SpuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProductController {

    private final CategoryService categoryService;
    private final SpuService spuService;

    @GetMapping("/categories/tree")
    public Result<List<CategoryTreeVO>> getCategoryTree() {
        return Result.ok(categoryService.getTree());
    }

    @GetMapping("/products/{id}")
    public Result<SpuDetailVO> getSpuDetail(@PathVariable("id") Long id) {
        return Result.ok(spuService.getSpuDetail(id));
    }
}

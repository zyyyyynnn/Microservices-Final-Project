package com.mallcloud.mallproduct.controller;

import com.mallcloud.mallcommon.response.Result;
import com.mallcloud.mallproduct.api.dto.ProductSaveDTO;
import com.mallcloud.mallproduct.service.SpuService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final SpuService spuService;

    @PostMapping
    public Result<Void> createProduct(@Validated @RequestBody ProductSaveDTO dto) {
        spuService.saveProduct(dto);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<Void> updateProduct(@PathVariable("id") Long id, @Validated @RequestBody ProductSaveDTO dto) {
        spuService.updateProduct(id, dto);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(@PathVariable("id") Long id) {
        spuService.deleteProduct(id);
        return Result.ok();
    }

    @PostMapping("/{id}/on")
    public Result<Void> onSale(@PathVariable("id") Long id) {
        spuService.updateStatus(id, 1);
        return Result.ok();
    }

    @PostMapping("/{id}/off")
    public Result<Void> offSale(@PathVariable("id") Long id) {
        spuService.updateStatus(id, 0);
        return Result.ok();
    }
}

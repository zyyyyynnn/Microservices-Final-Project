package com.mallcloud.mallproduct.controller;

import com.mallcloud.mallcommon.response.Result;
import com.mallcloud.mallproduct.api.vo.SkuVO;
import com.mallcloud.mallproduct.domain.Sku;
import com.mallcloud.mallproduct.service.SkuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.BeanUtils;

@RestController
@RequestMapping("/internal/products")
@RequiredArgsConstructor
public class InternalProductController {

    private final SkuService skuService;

    @GetMapping("/skus/{skuId}")
    public Result<SkuVO> getSku(@PathVariable("skuId") Long skuId) {
        Sku sku = skuService.getById(skuId);
        if (sku == null) {
            return Result.ok(null);
        }
        SkuVO skuVO = new SkuVO();
        BeanUtils.copyProperties(sku, skuVO);
        skuVO.setSkuId(sku.getId());
        return Result.ok(skuVO);
    }
}

package com.mallcloud.mallsearch.client.dto;

import lombok.Data;

import java.util.List;

/**
 * 商品服务返回的商品详情
 *
 * @author lisi
 * @since 2026-03-01
 */
@Data
public class ProductDetailDTO {

    private Long spuId;
    private String name;
    private String description;
    private String mainImage;
    private Long categoryId;
    private Integer status;
    private Integer sales;
    private String brand;
    private List<SkuDTO> skus;
}

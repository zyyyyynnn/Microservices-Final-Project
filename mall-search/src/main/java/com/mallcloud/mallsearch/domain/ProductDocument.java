package com.mallcloud.mallsearch.domain;

import lombok.Data;

import java.math.BigDecimal;

/**
 * ES 商品文档
 *
 * @author lisi
 * @since 2026-03-01
 */
@Data
public class ProductDocument {

    private Long spuId;
    private String name;
    private String description;
    private String mainImage;
    private Long categoryId;
    private String categoryName;
    private String brand;
    private BigDecimal price;
    private Integer sales;
    private Integer status;
}

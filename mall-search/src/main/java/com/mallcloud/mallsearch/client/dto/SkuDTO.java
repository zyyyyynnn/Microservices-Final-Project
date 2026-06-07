package com.mallcloud.mallsearch.client.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品 SKU 摘要
 *
 * @author lisi
 * @since 2026-03-01
 */
@Data
public class SkuDTO {

    private Long skuId;
    private String spec;
    private BigDecimal price;
    private Integer stock;
    private String image;
}

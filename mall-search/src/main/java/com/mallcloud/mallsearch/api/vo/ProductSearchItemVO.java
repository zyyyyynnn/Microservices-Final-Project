package com.mallcloud.mallsearch.api.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品搜索条目
 *
 * @author lisi
 * @since 2026-03-01
 */
@Data
public class ProductSearchItemVO {

    private Long spuId;
    private String name;
    private String highlightName;
    private BigDecimal price;
    private Integer sales;
    private String mainImage;
}

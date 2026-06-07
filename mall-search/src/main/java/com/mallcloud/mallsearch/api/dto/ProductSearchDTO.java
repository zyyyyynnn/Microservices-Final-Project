package com.mallcloud.mallsearch.api.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品搜索参数
 *
 * @author lisi
 * @since 2026-03-01
 */
@Data
public class ProductSearchDTO {

    private String keyword;
    private Long categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String sort = "_score";
    private Integer pageNum = 1;
    private Integer pageSize = 20;
}

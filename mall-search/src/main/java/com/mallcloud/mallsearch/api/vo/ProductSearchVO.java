package com.mallcloud.mallsearch.api.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 商品搜索结果
 *
 * @author lisi
 * @since 2026-03-01
 */
@Data
public class ProductSearchVO {

    private List<ProductSearchItemVO> list;
    private Long total;
    private Map<String, List<AggregationItemVO>> aggregations;
}

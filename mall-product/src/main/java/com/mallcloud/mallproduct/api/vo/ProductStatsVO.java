package com.mallcloud.mallproduct.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 商品统计响应
 *
 * @author lisi
 * @since 2026-03-01
 */
@Data
public class ProductStatsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long totalProducts;
    private List<TopProductVO> topProducts = Collections.emptyList();
}

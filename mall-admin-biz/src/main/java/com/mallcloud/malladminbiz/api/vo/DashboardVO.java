package com.mallcloud.malladminbiz.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * 后台看板响应
 *
 * @author zhaoliu
 * @since 2026-03-01
 */
@Data
public class DashboardVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long todayOrders;
    private BigDecimal todaySales;
    private Long totalProducts;
    private Long pendingOrders;
    private List<SalesTrendItemVO> salesTrend = Collections.emptyList();
    private List<TopProductVO> topProducts = Collections.emptyList();
}

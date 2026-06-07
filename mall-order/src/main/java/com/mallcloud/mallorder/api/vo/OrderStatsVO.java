package com.mallcloud.mallorder.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * 订单统计响应
 *
 * @author wangwu
 * @since 2026-03-01
 */
@Data
public class OrderStatsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long todayOrders;
    private BigDecimal todaySales;
    private Long pendingOrders;
    private List<SalesTrendItemVO> salesTrend = Collections.emptyList();
}

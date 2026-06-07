package com.mallcloud.malladminbiz.client.dto;

import com.mallcloud.malladminbiz.api.vo.SalesTrendItemVO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * 订单统计 DTO
 *
 * @author zhaoliu
 * @since 2026-03-01
 */
@Data
public class OrderStatsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long todayOrders;
    private BigDecimal todaySales;
    private Long pendingOrders;
    private List<SalesTrendItemVO> salesTrend = Collections.emptyList();
}

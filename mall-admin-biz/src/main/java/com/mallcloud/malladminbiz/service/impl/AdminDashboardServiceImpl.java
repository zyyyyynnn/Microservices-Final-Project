package com.mallcloud.malladminbiz.service.impl;

import com.mallcloud.malladminbiz.api.vo.DashboardVO;
import com.mallcloud.malladminbiz.client.OrderAdminClient;
import com.mallcloud.malladminbiz.client.ProductAdminClient;
import com.mallcloud.malladminbiz.client.dto.OrderStatsDTO;
import com.mallcloud.malladminbiz.client.dto.ProductStatsDTO;
import com.mallcloud.malladminbiz.service.AdminDashboardService;
import com.mallcloud.mallcommon.enums.ErrorCode;
import com.mallcloud.mallcommon.exception.BizException;
import com.mallcloud.mallcommon.response.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;

/**
 * 后台看板服务实现
 *
 * @author zhaoliu
 * @since 2026-03-01
 */
@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final OrderAdminClient orderAdminClient;
    private final ProductAdminClient productAdminClient;

    @Override
    public DashboardVO getDashboard() {
        OrderStatsDTO orderStats = requireSuccess(orderAdminClient.getOrderStats(), "订单统计查询失败");
        ProductStatsDTO productStats = requireSuccess(productAdminClient.getProductStats(), "商品统计查询失败");

        DashboardVO vo = new DashboardVO();
        vo.setTodayOrders(defaultLong(orderStats.getTodayOrders()));
        vo.setTodaySales(orderStats.getTodaySales() == null ? BigDecimal.ZERO : orderStats.getTodaySales());
        vo.setPendingOrders(defaultLong(orderStats.getPendingOrders()));
        vo.setSalesTrend(orderStats.getSalesTrend() == null ? Collections.emptyList() : orderStats.getSalesTrend());
        vo.setTotalProducts(defaultLong(productStats.getTotalProducts()));
        vo.setTopProducts(productStats.getTopProducts() == null ? Collections.emptyList() : productStats.getTopProducts());
        return vo;
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

    private <T> T requireSuccess(Result<T> result, String message) {
        if (result == null || !result.isSuccess() || result.getData() == null) {
            throw new BizException(ErrorCode.REMOTE_CALL_ERROR.getCode(), message);
        }
        return result.getData();
    }
}

package com.mallcloud.malladminbiz.service.impl;

import com.mallcloud.malladminbiz.api.vo.DashboardVO;
import com.mallcloud.malladminbiz.api.vo.TopProductVO;
import com.mallcloud.malladminbiz.client.OrderAdminClient;
import com.mallcloud.malladminbiz.client.ProductAdminClient;
import com.mallcloud.malladminbiz.client.dto.OrderStatsDTO;
import com.mallcloud.malladminbiz.client.dto.ProductStatsDTO;
import com.mallcloud.mallcommon.response.Result;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminDashboardServiceImplTest {

    private final OrderAdminClient orderAdminClient = mock(OrderAdminClient.class);
    private final ProductAdminClient productAdminClient = mock(ProductAdminClient.class);
    private final AdminDashboardServiceImpl service =
            new AdminDashboardServiceImpl(orderAdminClient, productAdminClient);

    @Test
    void getDashboardMergesOrderAndProductStats() {
        OrderStatsDTO orderStats = new OrderStatsDTO();
        orderStats.setTodayOrders(12L);
        orderStats.setTodaySales(new BigDecimal("3560.50"));
        orderStats.setPendingOrders(3L);
        ProductStatsDTO productStats = new ProductStatsDTO();
        productStats.setTotalProducts(8L);
        TopProductVO topProduct = new TopProductVO();
        topProduct.setSpuId(1001L);
        topProduct.setName("iPhone 15 Pro");
        topProduct.setSales(45);
        productStats.setTopProducts(List.of(topProduct));
        when(orderAdminClient.getOrderStats()).thenReturn(Result.ok(orderStats));
        when(productAdminClient.getProductStats()).thenReturn(Result.ok(productStats));

        DashboardVO dashboard = service.getDashboard();

        assertEquals(12L, dashboard.getTodayOrders());
        assertEquals(new BigDecimal("3560.50"), dashboard.getTodaySales());
        assertEquals(8L, dashboard.getTotalProducts());
        assertEquals(3L, dashboard.getPendingOrders());
        assertEquals(1, dashboard.getTopProducts().size());
    }
}

package com.mallcloud.malljob.service.impl;

import com.mallcloud.mallcommon.response.Result;
import com.mallcloud.malljob.client.InventoryJobClient;
import com.mallcloud.malljob.client.OrderJobClient;
import com.mallcloud.malljob.client.ProductJobClient;
import com.mallcloud.malljob.client.SearchJobClient;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JobTaskServiceImplTest {

    private final OrderJobClient orderJobClient = mock(OrderJobClient.class);
    private final InventoryJobClient inventoryJobClient = mock(InventoryJobClient.class);
    private final ProductJobClient productJobClient = mock(ProductJobClient.class);
    private final SearchJobClient searchJobClient = mock(SearchJobClient.class);
    private final JobTaskServiceImpl service = new JobTaskServiceImpl(
            orderJobClient,
            inventoryJobClient,
            productJobClient,
            searchJobClient);

    @Test
    void closeTimeoutOrdersDelegatesToOrderService() {
        when(orderJobClient.closeTimeoutOrders()).thenReturn(Result.ok(3));

        service.closeTimeoutOrders();

        verify(orderJobClient).closeTimeoutOrders();
    }

    @Test
    void reconcileInventoryDelegatesToInventoryService() {
        when(inventoryJobClient.reconcileStock()).thenReturn(Result.ok(2));

        service.reconcileInventory();

        verify(inventoryJobClient).reconcileStock();
    }

    @Test
    void syncAllProductsLoadsIdsAndSyncsSearchDocuments() {
        when(productJobClient.listOnSaleSpuIds()).thenReturn(Result.ok(List.of(1L, 2L)));
        when(searchJobClient.syncProduct(1L, 1)).thenReturn(Result.ok());
        when(searchJobClient.syncProduct(2L, 1)).thenReturn(Result.ok());

        service.syncAllProductsToSearch();

        verify(productJobClient).listOnSaleSpuIds();
        verify(searchJobClient).syncProduct(1L, 1);
        verify(searchJobClient).syncProduct(2L, 1);
    }
}

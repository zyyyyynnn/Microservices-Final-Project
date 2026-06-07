package com.mallcloud.mallmessage.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mallcloud.mallcommon.response.Result;
import com.mallcloud.mallmessage.api.dto.SeckillOrderCreateDTO;
import com.mallcloud.mallmessage.client.InventoryClient;
import com.mallcloud.mallmessage.client.OrderClient;
import com.mallcloud.mallmessage.client.SearchClient;
import com.mallcloud.mallmessage.client.SeckillClient;
import com.mallcloud.mallmessage.client.dto.OrderNoDTO;
import com.mallcloud.mallmessage.client.vo.SeckillOrderVO;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MessageDispatchServiceImplTest {

    private final OrderClient orderClient = mock(OrderClient.class);
    private final InventoryClient inventoryClient = mock(InventoryClient.class);
    private final SeckillClient seckillClient = mock(SeckillClient.class);
    private final SearchClient searchClient = mock(SearchClient.class);
    private final MessageDispatchServiceImpl service = new MessageDispatchServiceImpl(
            orderClient,
            inventoryClient,
            seckillClient,
            searchClient,
            new ObjectMapper());

    @Test
    void handlePayResultMarksOrderPaidAndDeductsInventory() {
        when(orderClient.markPaid("SO202606070001")).thenReturn(Result.ok());
        when(inventoryClient.deduct(any(OrderNoDTO.class))).thenReturn(Result.ok());

        service.handlePayResult("{\"orderNo\":\"SO202606070001\",\"tradeNo\":\"T001\",\"status\":\"SUCCESS\"}");

        verify(orderClient).markPaid("SO202606070001");
        verify(inventoryClient).deduct(any(OrderNoDTO.class));
    }

    @Test
    void handleStockRollbackReleasesInventoryByOrderNo() {
        when(inventoryClient.release(any(OrderNoDTO.class))).thenReturn(Result.ok());

        service.handleStockRollback("{\"orderNo\":\"SO202606070002\"}");

        verify(inventoryClient).release(any(OrderNoDTO.class));
    }

    @Test
    void handleSeckillRequestCreatesOrderAndMarksRequestSuccess() {
        SeckillOrderVO order = new SeckillOrderVO();
        order.setOrderNo("SK202606070001");
        when(orderClient.createSeckillOrder(any(SeckillOrderCreateDTO.class))).thenReturn(Result.ok(order));
        when(seckillClient.markSuccess(eq("1:2:req"), eq("SK202606070001"))).thenReturn(Result.ok());

        service.handleSeckillRequest("""
                {"requestId":"1:2:req","activityId":1,"userId":2,"skuId":9003,"quantity":1,"seckillPrice":4799.00}
                """);

        verify(orderClient).createSeckillOrder(any(SeckillOrderCreateDTO.class));
        verify(seckillClient).markSuccess("1:2:req", "SK202606070001");
    }

    @Test
    void handleEsSyncForwardsProductSyncMessage() {
        when(searchClient.syncProduct(1L, 1)).thenReturn(Result.ok());

        service.handleEsSync("{\"spuId\":1,\"status\":1}");

        verify(searchClient).syncProduct(1L, 1);
    }
}

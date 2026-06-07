package com.mallcloud.mallmessage.client;

import com.mallcloud.mallcommon.enums.ErrorCode;
import com.mallcloud.mallcommon.response.Result;
import com.mallcloud.mallmessage.api.dto.SeckillOrderCreateDTO;
import com.mallcloud.mallmessage.client.dto.OrderNoDTO;
import com.mallcloud.mallmessage.client.vo.SeckillOrderVO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MessageFeignFallbackFactoryTest {

    @Test
    void orderFallbackReturnsRemoteCallError() {
        OrderClient client = new OrderClientFallbackFactory().create(new RuntimeException("order down"));

        Result<Void> markPaid = client.markPaid("SO202606070001");
        Result<SeckillOrderVO> seckill = client.createSeckillOrder(seckillOrderCreateDTO());

        assertFalse(markPaid.isSuccess());
        assertEquals(ErrorCode.REMOTE_CALL_ERROR.getCode(), markPaid.getCode());
        assertFalse(seckill.isSuccess());
        assertEquals(ErrorCode.REMOTE_CALL_ERROR.getCode(), seckill.getCode());
    }

    @Test
    void inventoryFallbackReturnsRemoteCallError() {
        InventoryClient client = new InventoryClientFallbackFactory().create(new RuntimeException("inventory down"));

        Result<Void> deduct = client.deduct(new OrderNoDTO("SO202606070001"));
        Result<Void> release = client.release(new OrderNoDTO("SO202606070001"));

        assertFalse(deduct.isSuccess());
        assertEquals(ErrorCode.REMOTE_CALL_ERROR.getCode(), deduct.getCode());
        assertFalse(release.isSuccess());
        assertEquals(ErrorCode.REMOTE_CALL_ERROR.getCode(), release.getCode());
    }

    private SeckillOrderCreateDTO seckillOrderCreateDTO() {
        SeckillOrderCreateDTO dto = new SeckillOrderCreateDTO();
        dto.setRequestId("1:2:req");
        return dto;
    }
}

package com.mallcloud.mallmessage.client;

import com.mallcloud.mallcommon.enums.ErrorCode;
import com.mallcloud.mallcommon.response.Result;
import com.mallcloud.mallmessage.client.dto.OrderNoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InventoryClientFallbackFactory implements FallbackFactory<InventoryClient> {

    @Override
    public InventoryClient create(Throwable cause) {
        return new InventoryClient() {
            @Override
            public Result<Void> deduct(OrderNoDTO dto) {
                String orderNo = dto == null ? null : dto.getOrderNo();
                log.error("调用 mall-inventory 确认扣减库存失败, orderNo={}, error={}", orderNo, error(cause), cause);
                return Result.error(ErrorCode.REMOTE_CALL_ERROR.getCode(), "调用 mall-inventory 确认扣减库存失败");
            }

            @Override
            public Result<Void> release(OrderNoDTO dto) {
                String orderNo = dto == null ? null : dto.getOrderNo();
                log.error("调用 mall-inventory 释放库存失败, orderNo={}, error={}", orderNo, error(cause), cause);
                return Result.error(ErrorCode.REMOTE_CALL_ERROR.getCode(), "调用 mall-inventory 释放库存失败");
            }
        };
    }

    private String error(Throwable cause) {
        return cause == null ? "unknown" : cause.getMessage();
    }
}

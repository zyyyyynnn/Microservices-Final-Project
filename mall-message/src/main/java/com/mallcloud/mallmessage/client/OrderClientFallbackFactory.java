package com.mallcloud.mallmessage.client;

import com.mallcloud.mallcommon.enums.ErrorCode;
import com.mallcloud.mallcommon.response.Result;
import com.mallcloud.mallmessage.api.dto.SeckillOrderCreateDTO;
import com.mallcloud.mallmessage.client.vo.SeckillOrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderClientFallbackFactory implements FallbackFactory<OrderClient> {

    @Override
    public OrderClient create(Throwable cause) {
        return new OrderClient() {
            @Override
            public Result<Void> markPaid(String orderNo) {
                log.error("调用 mall-order 标记支付失败, orderNo={}, error={}", orderNo, error(cause), cause);
                return Result.error(ErrorCode.REMOTE_CALL_ERROR.getCode(), "调用 mall-order 标记支付失败");
            }

            @Override
            public Result<SeckillOrderVO> createSeckillOrder(SeckillOrderCreateDTO dto) {
                String requestId = dto == null ? null : dto.getRequestId();
                log.error("调用 mall-order 创建秒杀订单失败, requestId={}, error={}", requestId, error(cause), cause);
                return Result.error(ErrorCode.REMOTE_CALL_ERROR.getCode(), "调用 mall-order 创建秒杀订单失败");
            }
        };
    }

    private String error(Throwable cause) {
        return cause == null ? "unknown" : cause.getMessage();
    }
}

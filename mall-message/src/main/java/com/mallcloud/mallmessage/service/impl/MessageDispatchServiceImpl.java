package com.mallcloud.mallmessage.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mallcloud.mallcommon.enums.ErrorCode;
import com.mallcloud.mallcommon.exception.BizException;
import com.mallcloud.mallcommon.response.Result;
import com.mallcloud.mallmessage.api.dto.SeckillOrderCreateDTO;
import com.mallcloud.mallmessage.client.InventoryClient;
import com.mallcloud.mallmessage.client.OrderClient;
import com.mallcloud.mallmessage.client.SearchClient;
import com.mallcloud.mallmessage.client.SeckillClient;
import com.mallcloud.mallmessage.client.dto.OrderNoDTO;
import com.mallcloud.mallmessage.client.vo.SeckillOrderVO;
import com.mallcloud.mallmessage.service.MessageDispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * MQ 消息分发服务实现
 *
 * @author zhaoliu
 * @since 2026-03-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageDispatchServiceImpl implements MessageDispatchService {

    private final OrderClient orderClient;
    private final InventoryClient inventoryClient;
    private final SeckillClient seckillClient;
    private final SearchClient searchClient;
    private final ObjectMapper objectMapper;

    @Override
    public void handleOrderCreated(String message) {
        log.info("收到订单创建消息: {}", message);
    }

    @Override
    public void handlePayResult(String message) {
        Map<String, Object> payload = readMap(message);
        String orderNo = requireText(payload, "orderNo");
        String status = String.valueOf(payload.getOrDefault("status", ""));
        if (!"SUCCESS".equalsIgnoreCase(status)) {
            log.info("支付未成功，不触发订单支付完成 orderNo={} status={}", orderNo, status);
            return;
        }
        requireSuccess(orderClient.markPaid(orderNo), "订单支付状态更新失败");
        requireSuccess(inventoryClient.deduct(new OrderNoDTO(orderNo)), "库存确认扣减失败");
    }

    @Override
    public void handleStockRollback(String message) {
        String orderNo = requireText(readMap(message), "orderNo");
        requireSuccess(inventoryClient.release(new OrderNoDTO(orderNo)), "库存释放失败");
    }

    @Override
    public void handleSeckillRequest(String message) {
        SeckillOrderCreateDTO dto = readValue(message, SeckillOrderCreateDTO.class);
        try {
            SeckillOrderVO order = requireSuccess(orderClient.createSeckillOrder(dto), "秒杀订单创建失败");
            requireSuccess(seckillClient.markSuccess(dto.getRequestId(), order.getOrderNo()), "秒杀结果回写失败");
        } catch (RuntimeException e) {
            requireSuccess(seckillClient.markFailed(dto.getRequestId(), e.getMessage()), "秒杀失败结果回写失败");
            throw e;
        }
    }

    @Override
    public void handleEsSync(String message) {
        Map<String, Object> payload = readMap(message);
        Long spuId = Long.valueOf(String.valueOf(payload.get("spuId")));
        Integer status = Integer.valueOf(String.valueOf(payload.getOrDefault("status", "1")));
        requireSuccess(searchClient.syncProduct(spuId, status), "ES 同步转发失败");
    }

    private Map<String, Object> readMap(String message) {
        return readValue(message, new TypeReference<>() {});
    }

    private <T> T readValue(String message, Class<T> type) {
        try {
            return objectMapper.readValue(message, type);
        } catch (JsonProcessingException e) {
            throw new BizException(ErrorCode.PARAM_ERROR.getCode(), "消息格式错误");
        }
    }

    private <T> T readValue(String message, TypeReference<T> type) {
        try {
            return objectMapper.readValue(message, type);
        } catch (JsonProcessingException e) {
            throw new BizException(ErrorCode.PARAM_ERROR.getCode(), "消息格式错误");
        }
    }

    private String requireText(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        if (value == null || !org.springframework.util.StringUtils.hasText(String.valueOf(value))) {
            throw new BizException(ErrorCode.PARAM_ERROR.getCode(), key + " 不能为空");
        }
        return String.valueOf(value);
    }

    private <T> T requireSuccess(Result<T> result, String message) {
        if (result == null || !result.isSuccess()) {
            throw new BizException(ErrorCode.REMOTE_CALL_ERROR.getCode(), message);
        }
        return result.getData();
    }
}

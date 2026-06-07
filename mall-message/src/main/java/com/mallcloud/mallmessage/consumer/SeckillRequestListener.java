package com.mallcloud.mallmessage.consumer;

import com.mallcloud.mallmessage.service.MessageDispatchService;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * 秒杀请求消息消费者
 *
 * @author zhaoliu
 * @since 2026-03-01
 */
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(topic = "SECKILL_REQUEST", consumerGroup = "mall-message-seckill-request")
public class SeckillRequestListener implements RocketMQListener<String> {

    private final MessageDispatchService messageDispatchService;

    @Override
    public void onMessage(String message) {
        messageDispatchService.handleSeckillRequest(message);
    }
}

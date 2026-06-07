package com.mallcloud.mallmessage.consumer;

import com.mallcloud.mallmessage.service.MessageDispatchService;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * ES 同步消息消费者
 *
 * @author zhaoliu
 * @since 2026-03-01
 */
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(topic = "ES_SYNC", consumerGroup = "mall-message-es-sync")
public class EsSyncListener implements RocketMQListener<String> {

    private final MessageDispatchService messageDispatchService;

    @Override
    public void onMessage(String message) {
        messageDispatchService.handleEsSync(message);
    }
}

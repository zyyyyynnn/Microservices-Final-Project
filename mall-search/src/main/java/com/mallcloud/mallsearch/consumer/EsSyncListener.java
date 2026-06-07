package com.mallcloud.mallsearch.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mallcloud.mallsearch.mq.EsSyncMessage;
import com.mallcloud.mallsearch.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 商品 ES 同步消息消费者
 *
 * @author lisi
 * @since 2026-03-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(topic = "ES_SYNC", consumerGroup = "mall-search-es-sync")
public class EsSyncListener implements RocketMQListener<String> {

    private final SearchService searchService;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(String message) {
        EsSyncMessage syncMessage = parseMessage(message);
        searchService.syncProduct(syncMessage.getSpuId(), syncMessage.getStatus());
    }

    private EsSyncMessage parseMessage(String message) {
        if (!StringUtils.hasText(message)) {
            throw new IllegalArgumentException("ES_SYNC message is blank");
        }
        String text = message.trim();
        if (!text.startsWith("{")) {
            EsSyncMessage syncMessage = new EsSyncMessage();
            syncMessage.setSpuId(Long.valueOf(text));
            syncMessage.setStatus(1);
            return syncMessage;
        }
        try {
            return objectMapper.readValue(text, EsSyncMessage.class);
        } catch (JsonProcessingException e) {
            log.error("解析 ES_SYNC 消息失败 message={}", message, e);
            throw new IllegalArgumentException("ES_SYNC message invalid", e);
        }
    }
}

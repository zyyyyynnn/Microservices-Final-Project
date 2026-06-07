package com.mallcloud.mallmessage.service;

/**
 * MQ 消息分发服务
 *
 * @author zhaoliu
 * @since 2026-03-01
 */
public interface MessageDispatchService {

    /**
     * 处理订单创建消息
     */
    void handleOrderCreated(String message);

    /**
     * 处理支付结果消息
     */
    void handlePayResult(String message);

    /**
     * 处理库存回滚消息
     */
    void handleStockRollback(String message);

    /**
     * 处理秒杀异步下单消息
     */
    void handleSeckillRequest(String message);

    /**
     * 处理 ES 同步消息
     */
    void handleEsSync(String message);
}

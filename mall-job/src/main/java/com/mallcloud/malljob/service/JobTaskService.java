package com.mallcloud.malljob.service;

/**
 * 定时任务服务
 *
 * @author zhaoliu
 * @since 2026-03-01
 */
public interface JobTaskService {

    /**
     * 关闭超时未支付订单
     */
    int closeTimeoutOrders();

    /**
     * 库存对账
     */
    int reconcileInventory();

    /**
     * 全量同步商品到搜索索引
     */
    int syncAllProductsToSearch();
}

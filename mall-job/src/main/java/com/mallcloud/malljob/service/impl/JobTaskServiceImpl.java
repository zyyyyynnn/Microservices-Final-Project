package com.mallcloud.malljob.service.impl;

import com.mallcloud.mallcommon.enums.ErrorCode;
import com.mallcloud.mallcommon.exception.BizException;
import com.mallcloud.mallcommon.response.Result;
import com.mallcloud.malljob.client.InventoryJobClient;
import com.mallcloud.malljob.client.OrderJobClient;
import com.mallcloud.malljob.client.ProductJobClient;
import com.mallcloud.malljob.client.SearchJobClient;
import com.mallcloud.malljob.service.JobTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 定时任务服务实现
 *
 * @author zhaoliu
 * @since 2026-03-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobTaskServiceImpl implements JobTaskService {

    private final OrderJobClient orderJobClient;
    private final InventoryJobClient inventoryJobClient;
    private final ProductJobClient productJobClient;
    private final SearchJobClient searchJobClient;

    @Override
    public int closeTimeoutOrders() {
        Integer count = requireSuccess(orderJobClient.closeTimeoutOrders(), "关闭超时订单失败");
        log.info("[Job] 关闭超时订单 count={}", count);
        return count == null ? 0 : count;
    }

    @Override
    public int reconcileInventory() {
        Integer count = requireSuccess(inventoryJobClient.reconcileStock(), "库存对账失败");
        log.info("[Job] 库存对账完成 count={}", count);
        return count == null ? 0 : count;
    }

    @Override
    public int syncAllProductsToSearch() {
        List<Long> spuIds = requireSuccess(productJobClient.listOnSaleSpuIds(), "查询上架商品失败");
        int count = 0;
        for (Long spuId : spuIds == null ? Collections.<Long>emptyList() : spuIds) {
            requireSuccess(searchJobClient.syncProduct(spuId, 1), "同步商品搜索文档失败");
            count++;
        }
        log.info("[Job] ES 全量同步完成 count={}", count);
        return count;
    }

    private <T> T requireSuccess(Result<T> result, String message) {
        if (result == null || !result.isSuccess()) {
            throw new BizException(ErrorCode.REMOTE_CALL_ERROR.getCode(), message);
        }
        return result.getData();
    }
}

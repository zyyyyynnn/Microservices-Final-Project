package com.mallcloud.malladminbiz.service.impl;

import com.mallcloud.malladminbiz.api.dto.AdminOrderQueryDTO;
import com.mallcloud.malladminbiz.api.dto.ShipOrderDTO;
import com.mallcloud.malladminbiz.api.vo.AdminOrderVO;
import com.mallcloud.malladminbiz.client.OrderAdminClient;
import com.mallcloud.malladminbiz.service.AdminOrderService;
import com.mallcloud.mallcommon.enums.ErrorCode;
import com.mallcloud.mallcommon.exception.BizException;
import com.mallcloud.mallcommon.response.PageData;
import com.mallcloud.mallcommon.response.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 后台订单服务实现
 *
 * @author zhaoliu
 * @since 2026-03-01
 */
@Service
@RequiredArgsConstructor
public class AdminOrderServiceImpl implements AdminOrderService {

    private final OrderAdminClient orderAdminClient;

    @Override
    public PageData<AdminOrderVO> listOrders(AdminOrderQueryDTO query) {
        Result<PageData<AdminOrderVO>> result = orderAdminClient.listOrders(query);
        if (result == null || !result.isSuccess() || result.getData() == null) {
            throw new BizException(ErrorCode.REMOTE_CALL_ERROR.getCode(), "后台订单查询失败");
        }
        return result.getData();
    }

    @Override
    public void shipOrder(String orderNo, ShipOrderDTO dto) {
        Result<Void> result = orderAdminClient.shipOrder(orderNo, dto);
        if (result == null || !result.isSuccess()) {
            throw new BizException(ErrorCode.REMOTE_CALL_ERROR.getCode(), "订单发货失败");
        }
    }
}

package com.mallcloud.malladminbiz.service;

import com.mallcloud.malladminbiz.api.dto.AdminOrderQueryDTO;
import com.mallcloud.malladminbiz.api.dto.ShipOrderDTO;
import com.mallcloud.malladminbiz.api.vo.AdminOrderVO;
import com.mallcloud.mallcommon.response.PageData;

/**
 * 后台订单服务
 *
 * @author zhaoliu
 * @since 2026-03-01
 */
public interface AdminOrderService {

    /**
     * 查询后台订单列表
     */
    PageData<AdminOrderVO> listOrders(AdminOrderQueryDTO query);

    /**
     * 商家发货
     */
    void shipOrder(String orderNo, ShipOrderDTO dto);
}

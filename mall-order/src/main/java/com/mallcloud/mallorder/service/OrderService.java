package com.mallcloud.mallorder.service;

import com.mallcloud.mallorder.api.dto.CreateOrderDTO;
import com.mallcloud.mallorder.api.dto.AdminOrderQueryDTO;
import com.mallcloud.mallorder.api.dto.SeckillOrderCreateDTO;
import com.mallcloud.mallorder.api.dto.ShipOrderDTO;
import com.mallcloud.mallorder.api.vo.CreateOrderVO;
import com.mallcloud.mallorder.api.vo.AdminOrderVO;
import com.mallcloud.mallorder.api.vo.OrderVO;
import com.mallcloud.mallorder.api.vo.OrderStatsVO;
import com.mallcloud.mallorder.api.vo.SeckillOrderVO;
import com.mallcloud.mallcommon.response.PageData;

public abstract class OrderService {
    public abstract CreateOrderVO createOrder(Long userId, CreateOrderDTO dto);
    public abstract OrderVO getOrder(String orderNo, Long userId);
    public abstract void markPaid(String orderNo);
    public abstract SeckillOrderVO createSeckillOrder(SeckillOrderCreateDTO dto);
    public abstract OrderStatsVO getOrderStats();
    public abstract PageData<AdminOrderVO> listAdminOrders(AdminOrderQueryDTO query);
    public abstract void shipOrder(String orderNo, ShipOrderDTO dto);
    public abstract int closeTimeoutOrders();
}

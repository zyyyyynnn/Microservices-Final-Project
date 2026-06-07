package com.mallcloud.mallorder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mallcloud.mallcommon.enums.ErrorCode;
import com.mallcloud.mallcommon.exception.BizException;
import com.mallcloud.mallcommon.response.PageData;
import com.mallcloud.mallcommon.response.Result;
import com.mallcloud.mallorder.api.dto.AdminOrderQueryDTO;
import com.mallcloud.mallorder.api.dto.CreateOrderDTO;
import com.mallcloud.mallorder.api.dto.OrderItemDTO;
import com.mallcloud.mallorder.api.dto.SeckillOrderCreateDTO;
import com.mallcloud.mallorder.api.dto.ShipOrderDTO;
import com.mallcloud.mallorder.api.vo.AdminOrderVO;
import com.mallcloud.mallorder.api.vo.CreateOrderVO;
import com.mallcloud.mallorder.api.vo.OrderItemVO;
import com.mallcloud.mallorder.api.vo.OrderStatsVO;
import com.mallcloud.mallorder.api.vo.OrderVO;
import com.mallcloud.mallorder.api.vo.SalesTrendItemVO;
import com.mallcloud.mallorder.api.vo.SeckillOrderVO;
import com.mallcloud.mallorder.client.InventoryClient;
import com.mallcloud.mallorder.client.ProductClient;
import com.mallcloud.mallorder.client.dto.LockDTO;
import com.mallcloud.mallorder.client.dto.LockStockDTO;
import com.mallcloud.mallorder.client.dto.SkuDTO;
import com.mallcloud.mallorder.domain.OrderInfo;
import com.mallcloud.mallorder.domain.OrderItem;
import com.mallcloud.mallorder.mapper.OrderInfoMapper;
import com.mallcloud.mallorder.mapper.OrderItemMapper;
import com.mallcloud.mallorder.service.OrderService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends OrderService {

    private static final DateTimeFormatter TREND_DATE_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");

    private final OrderInfoMapper orderInfoMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;

    @Override
    @GlobalTransactional(name = "create-order", rollbackFor = Exception.class)
    public CreateOrderVO createOrder(Long userId, CreateOrderDTO dto) {
        String orderNo = "SO" + System.currentTimeMillis();
        
        List<OrderItem> orderItems = new ArrayList<>();
        List<LockDTO> lockDTOs = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (OrderItemDTO itemDto : dto.getItems()) {
            Result<SkuDTO> skuResult = productClient.getSku(itemDto.getSkuId());
            if (skuResult == null || !skuResult.isSuccess()) {
                throw new BizException(ErrorCode.REMOTE_CALL_ERROR.getCode(), "商品服务调用失败: " + itemDto.getSkuId());
            }
            if (skuResult.getData() == null) {
                throw new BizException(ErrorCode.PRODUCT_NOT_FOUND.getCode(), "商品不存在: " + itemDto.getSkuId());
            }
            SkuDTO sku = skuResult.getData();
            
            OrderItem item = new OrderItem();
            item.setOrderId(0L); // 将在插入后更新
            item.setOrderNo(orderNo);
            item.setSkuId(sku.getSkuId());
            item.setSpuId(sku.getSpuId() != null ? sku.getSpuId() : 0L); // 防止 null
            item.setSkuImage(sku.getImage());
            item.setSkuName(sku.getSpec() != null ? sku.getSpec() : "商品");
            item.setSpecJson("{}");
            item.setPrice(sku.getPrice());
            item.setQuantity(itemDto.getQuantity());
            item.setSubtotal(sku.getPrice().multiply(new BigDecimal(itemDto.getQuantity())));
            
            orderItems.add(item);
            totalAmount = totalAmount.add(item.getSubtotal());
            
            lockDTOs.add(new LockDTO(sku.getSkuId(), itemDto.getQuantity()));
        }
        
        Result<Void> lockResult = inventoryClient.lock(new LockStockDTO(orderNo, lockDTOs));
        if (lockResult == null || !lockResult.isSuccess()) {
            if (lockResult != null && lockResult.getCode() == ErrorCode.REMOTE_CALL_ERROR.getCode()) {
                throw new BizException(ErrorCode.REMOTE_CALL_ERROR.getCode(), "库存服务调用失败");
            }
            throw new BizException(ErrorCode.STOCK_NOT_ENOUGH.getCode(), "库存不足或锁定失败");
        }
        
        OrderInfo order = new OrderInfo();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setMerchantId(1L); // 默认商家
        order.setTotalAmount(totalAmount);
        order.setPayAmount(totalAmount);
        order.setFreightAmount(BigDecimal.ZERO);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setStatus(0); // 待支付
        order.setAddressJson("{\"addressId\":" + dto.getAddressId() + "}");
        order.setPayDeadline(LocalDateTime.now().plusMinutes(15));
        order.setRemark(dto.getRemark());
        
        orderInfoMapper.insert(order);
        
        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }
        
        log.info("创建订单 userId={} orderNo={}", userId, orderNo);
        
        CreateOrderVO vo = new CreateOrderVO();
        vo.setOrderNo(orderNo);
        vo.setTotalAmount(totalAmount);
        vo.setPayUrl("alipays://example?orderNo=" + orderNo);
        vo.setExpireTime(System.currentTimeMillis() + 15 * 60 * 1000);
        return vo;
    }

    public OrderVO getOrder(String orderNo, Long userId) {
        OrderInfo order = orderInfoMapper.selectOne(
            new LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getOrderNo, orderNo)
                .eq(OrderInfo::getUserId, userId)
        );
        
        if (order == null) {
            throw new BizException(ErrorCode.ORDER_NOT_FOUND);
        }
        
        List<OrderItem> items = orderItemMapper.selectList(
            new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, order.getId())
        );
        
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        
        List<OrderItemVO> itemVOs = items.stream().map(item -> {
            OrderItemVO itemVO = new OrderItemVO();
            BeanUtils.copyProperties(item, itemVO);
            return itemVO;
        }).collect(Collectors.toList());
        
        vo.setItems(itemVOs);
        return vo;
    }

    @Override
    public void markPaid(String orderNo) {
        int rows = orderInfoMapper.update(null, new LambdaUpdateWrapper<OrderInfo>()
                .eq(OrderInfo::getOrderNo, orderNo)
                .eq(OrderInfo::getStatus, 0)
                .set(OrderInfo::getStatus, 1)
                .set(OrderInfo::getGmtPay, LocalDateTime.now()));
        if (rows == 0) {
            OrderInfo order = orderInfoMapper.selectOne(new LambdaQueryWrapper<OrderInfo>()
                    .eq(OrderInfo::getOrderNo, orderNo));
            if (order == null) {
                throw new BizException(ErrorCode.ORDER_NOT_FOUND);
            }
            if (order.getStatus() != 1) {
                throw new BizException(ErrorCode.ORDER_STATUS_INVALID);
            }
        }
    }

    @Override
    @GlobalTransactional(name = "create-seckill-order", rollbackFor = Exception.class)
    public SeckillOrderVO createSeckillOrder(SeckillOrderCreateDTO dto) {
        String requestToken = "\"requestId\":\"" + dto.getRequestId() + "\"";
        OrderInfo existing = orderInfoMapper.selectOne(new LambdaQueryWrapper<OrderInfo>()
                .like(OrderInfo::getAddressJson, requestToken));
        if (existing != null) {
            return seckillOrderVO(existing.getOrderNo());
        }

        Result<SkuDTO> skuResult = productClient.getSku(dto.getSkuId());
        if (skuResult == null || !skuResult.isSuccess()) {
            throw new BizException(ErrorCode.REMOTE_CALL_ERROR.getCode(), "商品服务调用失败: " + dto.getSkuId());
        }
        if (skuResult.getData() == null) {
            throw new BizException(ErrorCode.PRODUCT_NOT_FOUND.getCode(), "商品不存在: " + dto.getSkuId());
        }
        SkuDTO sku = skuResult.getData();
        String orderNo = "SK" + System.currentTimeMillis();
        Result<Void> lockResult = inventoryClient.lock(new LockStockDTO(orderNo, List.of(new LockDTO(dto.getSkuId(), dto.getQuantity()))));
        if (lockResult == null || !lockResult.isSuccess()) {
            if (lockResult != null && lockResult.getCode() == ErrorCode.REMOTE_CALL_ERROR.getCode()) {
                throw new BizException(ErrorCode.REMOTE_CALL_ERROR.getCode(), "库存服务调用失败");
            }
            throw new BizException(ErrorCode.STOCK_NOT_ENOUGH.getCode(), "秒杀库存锁定失败");
        }

        BigDecimal price = dto.getSeckillPrice() == null ? sku.getPrice() : dto.getSeckillPrice();
        BigDecimal totalAmount = price.multiply(new BigDecimal(dto.getQuantity()));

        OrderInfo order = new OrderInfo();
        order.setOrderNo(orderNo);
        order.setUserId(dto.getUserId());
        order.setMerchantId(1L);
        order.setTotalAmount(totalAmount);
        order.setPayAmount(totalAmount);
        order.setFreightAmount(BigDecimal.ZERO);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setStatus(0);
        order.setAddressJson("{\"source\":\"SECKILL\",\"requestId\":\"" + dto.getRequestId() + "\"}");
        order.setPayDeadline(LocalDateTime.now().plusMinutes(15));
        order.setRemark("秒杀活动：" + dto.getActivityId());
        orderInfoMapper.insert(order);

        OrderItem item = new OrderItem();
        item.setOrderId(order.getId());
        item.setOrderNo(orderNo);
        item.setSkuId(sku.getSkuId());
        item.setSpuId(sku.getSpuId() == null ? 0L : sku.getSpuId());
        item.setSkuImage(sku.getImage());
        item.setSkuName(sku.getSpec() == null ? "秒杀商品" : sku.getSpec());
        item.setSpecJson("{}");
        item.setPrice(price);
        item.setQuantity(dto.getQuantity());
        item.setSubtotal(totalAmount);
        orderItemMapper.insert(item);
        log.info("创建秒杀订单 requestId={} orderNo={}", dto.getRequestId(), orderNo);
        return seckillOrderVO(orderNo);
    }

    private SeckillOrderVO seckillOrderVO(String orderNo) {
        SeckillOrderVO vo = new SeckillOrderVO();
        vo.setOrderNo(orderNo);
        return vo;
    }

    @Override
    public OrderStatsVO getOrderStats() {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        Long todayOrders = orderInfoMapper.selectCount(new LambdaQueryWrapper<OrderInfo>()
                .ge(OrderInfo::getGmtCreate, startOfToday));
        Long pendingOrders = orderInfoMapper.selectCount(new LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getStatus, 1));
        BigDecimal todaySales = sumPayAmount(startOfToday);

        OrderStatsVO vo = new OrderStatsVO();
        vo.setTodayOrders(todayOrders);
        vo.setPendingOrders(pendingOrders);
        vo.setTodaySales(todaySales);
        vo.setSalesTrend(buildSalesTrend());
        return vo;
    }

    @Override
    public PageData<AdminOrderVO> listAdminOrders(AdminOrderQueryDTO query) {
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 20 : query.getPageSize();
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<OrderInfo>()
                .eq(query.getStatus() != null, OrderInfo::getStatus, query.getStatus())
                .orderByDesc(OrderInfo::getGmtCreate);
        Page<OrderInfo> page = orderInfoMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<AdminOrderVO> list = page.getRecords().stream().map(order -> {
            AdminOrderVO vo = new AdminOrderVO();
            vo.setOrderNo(order.getOrderNo());
            vo.setUserId(order.getUserId());
            vo.setPayAmount(order.getPayAmount());
            vo.setStatus(order.getStatus());
            vo.setGmtCreate(order.getGmtCreate());
            return vo;
        }).toList();
        return PageData.of(list, page.getTotal(), pageNum, pageSize);
    }

    @Override
    public void shipOrder(String orderNo, ShipOrderDTO dto) {
        String remark = "发货：" + dto.getExpressCompany() + " " + dto.getExpressNo();
        int rows = orderInfoMapper.update(null, new LambdaUpdateWrapper<OrderInfo>()
                .eq(OrderInfo::getOrderNo, orderNo)
                .eq(OrderInfo::getStatus, 1)
                .set(OrderInfo::getStatus, 2)
                .set(OrderInfo::getRemark, remark));
        if (rows == 0) {
            OrderInfo order = orderInfoMapper.selectOne(new LambdaQueryWrapper<OrderInfo>()
                    .eq(OrderInfo::getOrderNo, orderNo));
            if (order == null) {
                throw new BizException(ErrorCode.ORDER_NOT_FOUND);
            }
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID);
        }
    }

    @Override
    public int closeTimeoutOrders() {
        return orderInfoMapper.update(null, new LambdaUpdateWrapper<OrderInfo>()
                .eq(OrderInfo::getStatus, 0)
                .lt(OrderInfo::getPayDeadline, LocalDateTime.now())
                .set(OrderInfo::getStatus, 4)
                .set(OrderInfo::getRemark, "订单超时自动关闭"));
    }

    private BigDecimal sumPayAmount(LocalDateTime startTime) {
        List<Map<String, Object>> rows = orderInfoMapper.selectMaps(new QueryWrapper<OrderInfo>()
                .select("COALESCE(SUM(pay_amount), 0) AS amount")
                .ge("gmt_pay", startTime)
                .in("status", List.of(1, 2, 3)));
        if (rows.isEmpty() || rows.get(0).get("amount") == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(String.valueOf(rows.get(0).get("amount")));
    }

    private List<SalesTrendItemVO> buildSalesTrend() {
        LocalDate startDate = LocalDate.now().minusDays(6);
        List<Map<String, Object>> rows = orderInfoMapper.selectMaps(new QueryWrapper<OrderInfo>()
                .select("DATE_FORMAT(gmt_pay, '%m-%d') AS trendDate", "COALESCE(SUM(pay_amount), 0) AS amount")
                .ge("gmt_pay", startDate.atStartOfDay())
                .in("status", List.of(1, 2, 3))
                .groupBy("DATE_FORMAT(gmt_pay, '%m-%d')")
                .orderByAsc("trendDate"));
        if (rows.isEmpty()) {
            return Collections.emptyList();
        }
        return rows.stream().map(row -> {
            SalesTrendItemVO item = new SalesTrendItemVO();
            item.setDate(String.valueOf(row.get("trendDate")));
            item.setAmount(new BigDecimal(String.valueOf(row.get("amount"))));
            return item;
        }).collect(Collectors.toList());
    }
}

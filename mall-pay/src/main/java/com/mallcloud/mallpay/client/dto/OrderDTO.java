package com.mallcloud.mallpay.client.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单服务返回的订单摘要
 *
 * @author wangwu
 * @since 2026-03-01
 */
@Data
public class OrderDTO {

    private String orderNo;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private Integer status;
}

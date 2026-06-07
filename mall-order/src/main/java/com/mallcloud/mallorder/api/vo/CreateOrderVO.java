package com.mallcloud.mallorder.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 创建订单响应
 *
 * @author wangwu
 * @since 2026-03-01
 */
@Data
public class CreateOrderVO implements Serializable {

    private String orderNo;
    private BigDecimal totalAmount;
    private String payUrl;
    private Long expireTime;
}

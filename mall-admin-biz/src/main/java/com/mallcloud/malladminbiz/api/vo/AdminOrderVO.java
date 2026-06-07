package com.mallcloud.malladminbiz.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 后台订单响应
 *
 * @author zhaoliu
 * @since 2026-03-01
 */
@Data
public class AdminOrderVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String orderNo;
    private Long userId;
    private BigDecimal payAmount;
    private Integer status;
    private LocalDateTime gmtCreate;
}

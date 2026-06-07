package com.mallcloud.mallpay.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录响应
 *
 * @author wangwu
 * @since 2026-03-01
 */
@Data
public class PayRecordVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String payNo;
    private String orderNo;
    private Long userId;
    private String payChannel;
    private BigDecimal payAmount;
    private Integer status;
    private String tradeNo;
    private LocalDateTime notifyTime;
    private LocalDateTime gmtCreate;
}

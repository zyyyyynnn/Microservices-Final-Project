package com.mallcloud.mallpay.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录实体
 *
 * @author wangwu
 * @since 2026-03-01
 */
@Data
@TableName("pay_record")
public class PayRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String payNo;
    private String orderNo;
    private Long userId;
    private String payChannel;
    private BigDecimal payAmount;
    private Integer status;
    private String tradeNo;
    private LocalDateTime notifyTime;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;
}

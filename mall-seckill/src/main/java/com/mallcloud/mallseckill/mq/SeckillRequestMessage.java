package com.mallcloud.mallseckill.mq;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 秒杀异步下单消息
 *
 * @author wangwu
 * @since 2026-03-01
 */
@Data
@Builder
public class SeckillRequestMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String requestId;
    private Long activityId;
    private Long userId;
    private Long skuId;
    private Integer quantity;
    private BigDecimal seckillPrice;
}

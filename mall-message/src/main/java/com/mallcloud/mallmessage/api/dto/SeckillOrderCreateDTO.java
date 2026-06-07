package com.mallcloud.mallmessage.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 秒杀异步建单请求
 *
 * @author zhaoliu
 * @since 2026-03-01
 */
@Data
public class SeckillOrderCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String requestId;
    private Long activityId;
    private Long userId;
    private Long skuId;
    private Integer quantity;
    private BigDecimal seckillPrice;
}

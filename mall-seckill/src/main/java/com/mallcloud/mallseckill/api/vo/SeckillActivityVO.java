package com.mallcloud.mallseckill.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀活动响应
 *
 * @author wangwu
 * @since 2026-03-01
 */
@Data
public class SeckillActivityVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long activityId;
    private String name;
    private Long skuId;
    private BigDecimal seckillPrice;
    private Integer totalStock;
    private Integer limitPerUser;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
}

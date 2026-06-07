package com.mallcloud.mallseckill.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 秒杀订单
 *
 * @author wangwu
 * @since 2026-03-01
 */
@Data
@TableName("seckill_order")
public class SeckillOrder {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long activityId;
    private Long userId;
    private Long skuId;
    private String orderNo;
    private String requestId;
    private Integer status;
    private LocalDateTime gmtCreate;
}

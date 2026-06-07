package com.mallcloud.mallseckill.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀活动
 *
 * @author wangwu
 * @since 2026-03-01
 */
@Data
@TableName("seckill_activity")
public class SeckillActivity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Long skuId;
    private BigDecimal seckillPrice;
    private Integer totalStock;
    private Integer limitPerUser;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private LocalDateTime gmtCreate;
}

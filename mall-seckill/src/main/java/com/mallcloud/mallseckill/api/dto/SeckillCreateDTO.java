package com.mallcloud.mallseckill.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.io.Serializable;

/**
 * 秒杀下单请求
 *
 * @author wangwu
 * @since 2026-03-01
 */
@Data
public class SeckillCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "skuId 不能为空")
    private Long skuId;

    @NotNull(message = "数量不能为空")
    @Positive(message = "数量必须大于 0")
    private Integer quantity;
}

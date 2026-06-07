package com.mallcloud.mallorder.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.io.Serializable;

/**
 * 订单项 DTO
 *
 * @author wangwu
 * @since 2026-03-01
 */
@Data
public class OrderItemDTO implements Serializable {

    @NotNull(message = "skuId 不能为空")
    private Long skuId;

    @NotNull(message = "数量不能为空")
    @Positive(message = "数量必须大于 0")
    private Integer quantity;
}

package com.mallcloud.mallcart.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartItemDTO {
    @NotNull(message = "skuId不能为空")
    private Long skuId;
    
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为1")
    private Integer quantity;
}

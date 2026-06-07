package com.mallcloud.mallorder.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建订单请求
 *
 * @author wangwu
 * @since 2026-03-01
 */
@Data
public class CreateOrderDTO implements Serializable {

    @NotNull(message = "收货地址 ID 不能为空")
    private Long addressId;

    @NotEmpty(message = "订单项不能为空")
    @Size(max = 50, message = "单次最多 50 件商品")
    @Valid
    private List<OrderItemDTO> items;

    private String remark;
}

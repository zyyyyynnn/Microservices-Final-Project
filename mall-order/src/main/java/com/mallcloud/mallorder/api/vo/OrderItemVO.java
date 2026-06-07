package com.mallcloud.mallorder.api.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItemVO {
    private Long skuId;
    private Long spuId;
    private String skuImage;
    private String skuName;
    private String specJson;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
}

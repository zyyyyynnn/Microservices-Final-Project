package com.mallcloud.mallcart.api.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CartItemVO {
    private Long skuId;
    private String skuName;
    private String skuImage;
    private BigDecimal price;
    private Integer quantity;
    private Boolean selected;
    private BigDecimal subtotal;
}

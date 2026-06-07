package com.mallcloud.mallcart.client.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SkuDTO {
    private Long skuId;
    private String spec;
    private BigDecimal price;
    private Integer stock;
    private String image;
}

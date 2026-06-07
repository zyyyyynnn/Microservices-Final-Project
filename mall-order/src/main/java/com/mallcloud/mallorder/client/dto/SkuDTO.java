package com.mallcloud.mallorder.client.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SkuDTO {
    private Long skuId;
    private Long spuId;
    private String spec;
    private BigDecimal price;
    private Integer stock;
    private String image;
}

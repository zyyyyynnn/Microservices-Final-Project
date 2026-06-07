package com.mallcloud.mallproduct.api.vo;
import lombok.Data;
import java.math.BigDecimal;
@Data
public class SkuVO {
    private Long skuId;
    private String spec;
    private BigDecimal price;
    private Integer stock;
    private String image;
}

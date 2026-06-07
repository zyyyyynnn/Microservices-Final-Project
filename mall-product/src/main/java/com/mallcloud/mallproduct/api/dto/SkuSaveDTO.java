package com.mallcloud.mallproduct.api.dto;
import lombok.Data;
import java.math.BigDecimal;
@Data
public class SkuSaveDTO {
    private String specJson;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String image;
    private Integer weight;
    private String barcode;
}

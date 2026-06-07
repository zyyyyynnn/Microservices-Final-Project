package com.mallcloud.mallproduct.api.vo;
import lombok.Data;
import java.util.List;
@Data
public class SpuDetailVO {
    private Long spuId;
    private String name;
    private String description;
    private String mainImage;
    private Long categoryId;
    private String brand;
    private Integer status;
    private Integer sales;
    private List<SkuVO> skus;
    private List<SpuAttrVO> attrs;
}

package com.mallcloud.mallproduct.api.dto;
import lombok.Data;
import java.util.List;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
@Data
public class ProductSaveDTO {
    @NotBlank
    private String name;
    private String description;
    private String mainImage;
    @NotNull
    private Long categoryId;
    private String brand;
    @NotNull
    private Long merchantId;
    private List<SkuSaveDTO> skus;
    private List<SpuAttrSaveDTO> attrs;
}

package com.mallcloud.mallproduct.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sku")
public class Sku {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long spuId;
    private String specJson;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String image;
    private Integer weight;
    private String barcode;
    private Integer status;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;
}

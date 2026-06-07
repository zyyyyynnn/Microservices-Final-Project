package com.mallcloud.mallproduct.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("spu")
public class Spu {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String mainImage;
    private Long categoryId;
    private String brand;
    private Long merchantId;
    private Integer status;
    private Integer sales;
    private Integer viewCount;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;
}

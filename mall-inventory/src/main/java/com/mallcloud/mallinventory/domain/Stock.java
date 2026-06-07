package com.mallcloud.mallinventory.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("stock")
public class Stock {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long skuId;
    private Integer total;
    private Integer locked;
    private Integer available;
    private Integer version;
    private Date gmtModified;
}

package com.mallcloud.mallproduct.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("spu_attr")
public class SpuAttr {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long spuId;
    private String attrName;
    private String attrValue;
}

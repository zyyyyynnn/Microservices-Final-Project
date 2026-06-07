package com.mallcloud.mallproduct.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("spu_img")
public class SpuImg {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long spuId;
    private String url;
    private Integer sort;
    private Integer isMain;
}

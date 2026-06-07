package com.mallcloud.mallinventory.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("stock_log")
public class StockLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long skuId;
    @TableField("`change`")
    private Integer changeQty;
    private String type;
    private String refNo;
    private String remark;
    private Date gmtCreate;
}

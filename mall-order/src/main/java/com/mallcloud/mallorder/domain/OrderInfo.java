package com.mallcloud.mallorder.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order_info")
public class OrderInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private Long merchantId;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private BigDecimal freightAmount;
    private BigDecimal discountAmount;
    private Integer status; // 0待付 1已付 2已发 3完成 4取消 5退款
    private String addressJson;
    private LocalDateTime payDeadline;
    private String remark;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtPay;
    private LocalDateTime gmtModified;
}

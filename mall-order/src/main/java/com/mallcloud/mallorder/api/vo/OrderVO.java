package com.mallcloud.mallorder.api.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderVO {
    private String orderNo;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private BigDecimal freightAmount;
    private Integer status;
    private String addressJson;
    private LocalDateTime gmtCreate;
    private List<OrderItemVO> items;
}

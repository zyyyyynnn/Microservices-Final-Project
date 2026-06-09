package com.mallcloud.mallinventory.api.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class StockVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long skuId;
    private Integer total;
    private Integer locked;
    private Integer available;
}

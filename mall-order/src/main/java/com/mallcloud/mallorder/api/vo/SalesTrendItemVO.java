package com.mallcloud.mallorder.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 销售趋势项
 *
 * @author wangwu
 * @since 2026-03-01
 */
@Data
public class SalesTrendItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String date;
    private BigDecimal amount;
}

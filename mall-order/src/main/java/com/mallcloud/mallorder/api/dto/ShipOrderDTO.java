package com.mallcloud.mallorder.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 订单发货请求
 *
 * @author wangwu
 * @since 2026-03-01
 */
@Data
public class ShipOrderDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String expressNo;
    private String expressCompany;
}

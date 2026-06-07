package com.mallcloud.malladminbiz.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 后台订单发货请求
 *
 * @author zhaoliu
 * @since 2026-03-01
 */
@Data
public class ShipOrderDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String expressNo;
    private String expressCompany;
}

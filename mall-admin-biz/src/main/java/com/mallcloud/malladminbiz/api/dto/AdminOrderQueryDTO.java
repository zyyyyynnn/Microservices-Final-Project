package com.mallcloud.malladminbiz.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 后台订单查询参数
 *
 * @author zhaoliu
 * @since 2026-03-01
 */
@Data
public class AdminOrderQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer status;
    private Integer pageNum = 1;
    private Integer pageSize = 20;
}

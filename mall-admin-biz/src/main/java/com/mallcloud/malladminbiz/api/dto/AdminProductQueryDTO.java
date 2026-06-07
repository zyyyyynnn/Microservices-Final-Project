package com.mallcloud.malladminbiz.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 后台商品查询参数
 *
 * @author zhaoliu
 * @since 2026-03-01
 */
@Data
public class AdminProductQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String keyword;
    private Integer status;
    private Integer pageNum = 1;
    private Integer pageSize = 20;
}

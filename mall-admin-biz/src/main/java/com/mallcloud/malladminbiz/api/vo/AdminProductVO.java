package com.mallcloud.malladminbiz.api.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 后台商品响应
 *
 * @author zhaoliu
 * @since 2026-03-01
 */
@Data
public class AdminProductVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long spuId;
    private String name;
    private String mainImage;
    private Long categoryId;
    private String brand;
    private Integer status;
    private Integer sales;
}

package com.mallcloud.malladminbiz.api.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 热销商品项
 *
 * @author zhaoliu
 * @since 2026-03-01
 */
@Data
public class TopProductVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long spuId;
    private String name;
    private Integer sales;
}

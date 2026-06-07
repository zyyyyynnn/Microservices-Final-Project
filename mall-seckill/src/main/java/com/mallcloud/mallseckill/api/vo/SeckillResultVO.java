package com.mallcloud.mallseckill.api.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 秒杀结果响应
 *
 * @author wangwu
 * @since 2026-03-01
 */
@Data
public class SeckillResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer status;
    private String orderNo;
    private String message;
}

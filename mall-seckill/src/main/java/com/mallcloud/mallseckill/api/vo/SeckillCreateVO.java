package com.mallcloud.mallseckill.api.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 秒杀下单响应
 *
 * @author wangwu
 * @since 2026-03-01
 */
@Data
@Builder
public class SeckillCreateVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String requestId;
    private String resultUrl;
}

package com.mallcloud.mallmessage.client.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 秒杀订单响应
 *
 * @author zhaoliu
 * @since 2026-03-01
 */
@Data
public class SeckillOrderVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String orderNo;
}

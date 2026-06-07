package com.mallcloud.mallmessage.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 订单号请求
 *
 * @author zhaoliu
 * @since 2026-03-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderNoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String orderNo;
}

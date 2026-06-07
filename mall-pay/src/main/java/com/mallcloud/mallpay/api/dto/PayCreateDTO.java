package com.mallcloud.mallpay.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 发起支付请求
 *
 * @author wangwu
 * @since 2026-03-01
 */
@Data
public class PayCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    @NotBlank(message = "支付渠道不能为空")
    private String payChannel;
}

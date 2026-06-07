package com.mallcloud.mallpay.api.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 发起支付响应
 *
 * @author wangwu
 * @since 2026-03-01
 */
@Data
@Builder
public class PayCreateVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String payNo;
    private String payUrl;
    private String payFormHtml;
}

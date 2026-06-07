package com.mallcloud.mallpay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付宝沙箱配置
 *
 * @author wangwu
 * @since 2026-03-01
 */
@Data
@Component
@ConfigurationProperties(prefix = "mallcloud.pay.alipay")
public class AlipayProperties {

    private String gatewayUrl = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    private String appId;
    private String privateKey;
    private String alipayPublicKey;
    private String notifyUrl;
    private String returnUrl;
    private String signType = "RSA2";
    private String charset = "UTF-8";
    private String format = "json";
}

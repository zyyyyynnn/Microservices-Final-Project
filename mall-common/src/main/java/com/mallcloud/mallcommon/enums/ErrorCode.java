package com.mallcloud.mallcommon.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业务错误码
 *
 * @author zhangsan
 * @since 2026-03-01
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    SUCCESS(0, "ok"),

    // 通用 1xxxx
    PARAM_ERROR(10001, "参数校验失败"),
    REMOTE_CALL_ERROR(10002, "远程调用失败"),
    SYSTEM_ERROR(10003, "系统内部错误"),

    // 认证 2xxxx
    UNAUTHORIZED(20100, "未登录"),
    TOKEN_EXPIRED(20101, "Token 已过期"),
    TOKEN_INVALID(20102, "Token 无效"),
    FORBIDDEN(20103, "无权限"),

    // 商品 3xxxx
    PRODUCT_NOT_FOUND(30100, "商品不存在"),
    PRODUCT_OFF_SHELF(30101, "商品已下架"),

    // 库存 4xxxx
    STOCK_NOT_ENOUGH(40100, "库存不足"),
    STOCK_LOCK_FAIL(40101, "库存锁定失败"),

    // 订单 4xxxx
    ORDER_NOT_FOUND(40200, "订单不存在"),
    ORDER_STATUS_INVALID(40201, "订单状态非法"),
    ORDER_TX_FAIL(40202, "分布式事务失败"),

    // 支付 4xxxx
    PAY_FAIL(40300, "支付失败"),

    // 秒杀 4xxxx
    SECKILL_STOCK_EMPTY(40400, "秒杀已售罄"),
    SECKILL_NOT_START(40401, "秒杀未开始"),
    SECKILL_END(40402, "秒杀已结束"),
    SECKILL_LIMIT(40403, "已达购买上限"),

    // 系统 5xxxx
    DEGRADE(50001, "服务降级中，请稍后重试"),
    RATE_LIMIT(50002, "请求过于频繁");

    private final int code;
    private final String message;
}

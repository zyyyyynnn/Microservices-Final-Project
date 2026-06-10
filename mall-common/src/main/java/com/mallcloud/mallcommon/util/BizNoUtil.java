package com.mallcloud.mallcommon.util;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 业务号生成工具
 *
 * @author zhangsan
 * @since 2026-03-01
 */
public final class BizNoUtil {

    private BizNoUtil() {}

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 订单号：SO + yyyyMMdd + 8 位雪花
     */
    public static String generateOrderNo() {
        return "SO" + LocalDate.now().format(DATE_FMT) + IdUtil.getSnowflake(1, 1).nextIdStr().substring(0, 8);
    }

    /**
     * 秒杀订单号：SK + 完整雪花 ID
     */
    public static String generateSeckillOrderNo() {
        return "SK" + IdUtil.getSnowflake(1, 1).nextIdStr();
    }

    /**
     * 支付单号：PAY + yyyyMMdd + 8 位雪花
     */
    public static String generatePayNo() {
        return "PAY" + LocalDate.now().format(DATE_FMT) + IdUtil.getSnowflake(1, 1).nextIdStr().substring(0, 8);
    }

    /**
     * 退款单号：RF + yyyyMMdd + 8 位雪花
     */
    public static String generateRefundNo() {
        return "RF" + LocalDate.now().format(DATE_FMT) + IdUtil.getSnowflake(1, 1).nextIdStr().substring(0, 8);
    }

    /**
     * 秒杀请求号（防重）
     */
    public static String generateRequestId(Long userId, Long activityId) {
        return StrUtil.format("{}:{}:{}", activityId, userId, IdUtil.fastSimpleUUID());
    }
}

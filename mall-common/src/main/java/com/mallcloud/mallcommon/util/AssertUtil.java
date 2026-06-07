package com.mallcloud.mallcommon.util;

import com.mallcloud.mallcommon.exception.BizException;
import org.springframework.util.StringUtils;

/**
 * 参数校验工具
 *
 * @author zhangsan
 * @since 2026-03-01
 */
public final class AssertUtil {

    private AssertUtil() {}

    public static void notNull(Object obj, String name) {
        if (obj == null) throw new BizException(name + "不能为空");
    }

    public static void notBlank(String str, String name) {
        if (!StringUtils.hasText(str)) throw new BizException(name + "不能为空");
    }

    public static void isTrue(boolean condition, String message) {
        if (!condition) throw new BizException(message);
    }

    public static void positive(Long value, String name) {
        if (value == null || value <= 0) throw new BizException(name + "必须为正数");
    }
}

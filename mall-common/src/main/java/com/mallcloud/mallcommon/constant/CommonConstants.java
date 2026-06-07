package com.mallcloud.mallcommon.constant;

/**
 * 通用常量
 *
 * @author zhangsan
 * @since 2026-03-01
 */
public final class CommonConstants {

    private CommonConstants() {}

    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_NAME = "X-User-Name";
    public static final String HEADER_USER_ROLES = "X-User-Roles";
    public static final String HEADER_TRACE_ID = "X-Trace-Id";

    public static final long DEFAULT_PAGE_NUM = 1L;
    public static final long DEFAULT_PAGE_SIZE = 20L;
    public static final long MAX_PAGE_SIZE = 200L;
}

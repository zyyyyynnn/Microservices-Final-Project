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
    /** 服务间内部调用鉴权 header（Feign 拦截器自动注入；服务 controller 校验） */
    public static final String HEADER_INTERNAL_TOKEN = "X-Internal-Token";
    public static final String JWT_CLAIM_TOKEN_TYPE = "tokenType";
    public static final String JWT_TOKEN_TYPE_ACCESS = "access";
    public static final String JWT_TOKEN_TYPE_REFRESH = "refresh";
    public static final String JWT_BLACKLIST_PREFIX = "mall:jwt:blacklist:";

    public static final long DEFAULT_PAGE_NUM = 1L;
    public static final long DEFAULT_PAGE_SIZE = 20L;
    public static final long MAX_PAGE_SIZE = 200L;
}

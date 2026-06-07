package com.mallcloud.mallcommon.util;

import com.mallcloud.mallcommon.constant.CommonConstants;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 当前用户上下文
 *
 * @author zhangsan
 * @since 2026-03-01
 */
public final class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_NAME = new ThreadLocal<>();
    private static final ThreadLocal<String> ROLES = new ThreadLocal<>();

    private UserContext() {}

    public static void setUserId(Long id) { USER_ID.set(id); }
    public static Long getUserId() { return USER_ID.get(); }

    public static void setUserName(String name) { USER_NAME.set(name); }
    public static String getUserName() { return USER_NAME.get(); }

    public static void setRoles(String roles) { ROLES.set(roles); }
    public static String getRoles() { return ROLES.get(); }

    public static Long requireUserId() {
        Long id = USER_ID.get();
        if (id == null) throw new com.mallcloud.mallcommon.exception.BizException(20100, "未登录");
        return id;
    }

    public static void clear() {
        USER_ID.remove();
        USER_NAME.remove();
        ROLES.remove();
    }

    public static Long parseUserIdFromHeader(HttpServletRequest req) {
        String v = req.getHeader(CommonConstants.HEADER_USER_ID);
        return v == null ? null : Long.valueOf(v);
    }
}

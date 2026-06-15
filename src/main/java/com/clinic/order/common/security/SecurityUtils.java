package com.clinic.order.common.security;

import jakarta.servlet.http.HttpServletRequest;

public class SecurityUtils {
    public static final String ATTR_USER = "current_user";
    public static final String ATTR_USER_ID = "current_user_id";
    public static final String ATTR_ROLE_TYPE = "current_role_type";

    private SecurityUtils() {}

    public static SessionUser currentUser(HttpServletRequest request) {
        Object user = request.getAttribute(ATTR_USER);
        return user instanceof SessionUser sessionUser ? sessionUser : null;
    }

    public static Long currentUserId(HttpServletRequest request) {
        Object userId = request.getAttribute(ATTR_USER_ID);
        if (userId instanceof Long id) return id;
        throw new IllegalStateException("请先登录");
    }

    public static Integer currentRoleType(HttpServletRequest request) {
        Object role = request.getAttribute(ATTR_ROLE_TYPE);
        return role instanceof Integer r ? r : null;
    }

    public static boolean isAdmin(HttpServletRequest request) {
        return Integer.valueOf(3).equals(currentRoleType(request));
    }
}
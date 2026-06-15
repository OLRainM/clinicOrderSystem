package com.clinic.order.common.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class SecurityUtils {
    public static final String SESSION_USER_KEY = "LOGIN_USER";

    private SecurityUtils() {}

    public static SessionUser currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        Object user = session.getAttribute(SESSION_USER_KEY);
        return user instanceof SessionUser sessionUser ? sessionUser : null;
    }

    public static Long currentUserId(HttpServletRequest request) {
        SessionUser user = currentUser(request);
        if (user == null || user.getUserId() == null) {
            throw new IllegalStateException("请先登录");
        }
        return user.getUserId();
    }

    public static boolean isAdmin(HttpServletRequest request) {
        SessionUser user = currentUser(request);
        return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
    }
}

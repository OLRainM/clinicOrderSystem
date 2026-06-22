package com.clinic.order.common.security;

import com.clinic.order.user.model.SysUser;
import com.clinic.order.user.service.AuthTokenService;
import com.clinic.order.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final AuthTokenService tokenService;
    private final UserService userService;

    public AuthInterceptor(AuthTokenService tokenService, UserService userService) {
        this.tokenService = tokenService;
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Optional<Long> userIdOpt = tokenService.verify(request);
        if (userIdOpt.isEmpty()) {
            return reject(request, response, HttpServletResponse.SC_UNAUTHORIZED, "请先登录");
        }
        SysUser user = userService.findActiveUser(userIdOpt.get());
        SessionUser sessionUser = new SessionUser(user.getId(), user.getPhone(), user.getRoleType());
        request.setAttribute(SecurityUtils.ATTR_USER, sessionUser);
        request.setAttribute(SecurityUtils.ATTR_USER_ID, user.getId());
        request.setAttribute(SecurityUtils.ATTR_ROLE_TYPE, user.getRoleType());
        RequireRole requireRole = findRequireRole(handler);
        if (requireRole != null && user.getRoleType() != requireRole.value()) {
            return reject(request, response, HttpServletResponse.SC_FORBIDDEN, "权限不足");
        }
        return true;
    }

    private RequireRole findRequireRole(Object handler) {
        if (!(handler instanceof HandlerMethod hm)) return null;
        RequireRole methodRole = hm.getMethodAnnotation(RequireRole.class);
        return methodRole != null ? methodRole : hm.getBeanType().getAnnotation(RequireRole.class);
    }

    private boolean reject(HttpServletRequest request, HttpServletResponse response, int status, String message) throws Exception {
        if (isAjax(request)) {
            write(response, status, message);
        } else {
            response.sendRedirect(request.getRequestURI().startsWith("/admin/") ? "/admin/login" : "/login");
        }
        return false;
    }

    private boolean isAjax(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return request.getRequestURI().startsWith("/api/") || (accept != null && accept.contains("application/json"));
    }

    private void write(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"success\":false,\"message\":\"" + message + "\"}");
    }
}
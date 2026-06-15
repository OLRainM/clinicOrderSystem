package com.clinic.order.common.security;

import com.clinic.order.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @PostMapping("/mock-login")
    public ApiResponse<SessionUser> mockLogin(@RequestParam Long userId,
                                              @RequestParam(defaultValue = "patient") String username,
                                              @RequestParam(defaultValue = "PATIENT") String role,
                                              HttpServletRequest request) {
        SessionUser user = new SessionUser(userId, username, role);
        request.getSession(true).setAttribute(SecurityUtils.SESSION_USER_KEY, user);
        return ApiResponse.ok("登录成功", user);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        request.getSession(true).invalidate();
        return ApiResponse.ok("已退出", null);
    }

    @GetMapping("/me")
    public ApiResponse<SessionUser> me(HttpServletRequest request) {
        return ApiResponse.ok("当前用户", SecurityUtils.currentUser(request));
    }
}

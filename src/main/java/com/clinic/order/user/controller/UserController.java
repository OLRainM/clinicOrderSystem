package com.clinic.order.user.controller;

import com.clinic.order.common.dto.ApiResponse;
import com.clinic.order.common.security.SessionUser;
import com.clinic.order.user.dto.*;
import com.clinic.order.user.model.SysUser;
import com.clinic.order.user.service.AuthTokenService;
import com.clinic.order.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class UserController {
    private final UserService userService;
    @Value("${clinic.admin.secret-key:ClinicAdmin@2026}")
    private String adminSecretKey;


    private final AuthTokenService tokenService;

    public UserController(UserService userService, AuthTokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @PostMapping("/register/patient")
    public ApiResponse<Map<String, Object>> registerPatient(@RequestBody @Valid PatientRegisterRequest req) {
        return ApiResponse.ok("患者注册成功", Map.of("userId", userService.registerPatient(req)));
    }

    @PostMapping("/login")
    public ApiResponse<SessionUser> login(@RequestBody @Valid LoginRequest req, HttpServletResponse response) {
        SysUser user = userService.login(req.getPhone(), req.getPassword());
        tokenService.issueToken(user.getId(), response);
        return ApiResponse.ok("登录成功", new SessionUser(user.getId(), user.getPhone(), user.getRoleType()));
    }

    @PostMapping("/admin-key-login")
    public ApiResponse<SessionUser> adminKeyLogin(@RequestBody @Valid AdminKeyLoginRequest req,
                                                   HttpServletResponse response) {
        if (!adminSecretKey.equals(req.getSecretKey())) {
            return ApiResponse.fail("管理员秘钥错误");
        }
        SysUser admin = userService.findAdminUser();
        tokenService.issueToken(admin.getId(), response);
        return ApiResponse.ok("管理员登录成功", new SessionUser(admin.getId(), admin.getPhone(), admin.getRoleType()));
    }


    @PostMapping("/mock-login")
    public ApiResponse<SessionUser> mockLogin(@RequestParam Long userId,
                                              @RequestParam(defaultValue = "patient") String username,
                                              @RequestParam(defaultValue = "PATIENT") String role,
                                              HttpServletResponse response) {
        int roleType = switch (role.toUpperCase()) {
            case "DOCTOR" -> UserService.ROLE_DOCTOR;
            case "ADMIN" -> UserService.ROLE_ADMIN;
            default -> UserService.ROLE_PATIENT;
        };
        tokenService.issueToken(userId, response);
        return ApiResponse.ok("登录成功", new SessionUser(userId, username, roleType));
    }

    @GetMapping("/me")
    public ApiResponse<SessionUser> me(HttpServletRequest request) {
        Long userId = tokenService.verify(request).orElseThrow(() -> new IllegalStateException("请先登录"));
        SysUser user = userService.findActiveUser(userId);
        return ApiResponse.ok("当前用户", new SessionUser(user.getId(), user.getPhone(), user.getRoleType()));
    }


    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        tokenService.logout(request, response);
        return ApiResponse.ok("已退出", null);
    }
}

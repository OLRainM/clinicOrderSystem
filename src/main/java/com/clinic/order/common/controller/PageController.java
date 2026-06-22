package com.clinic.order.common.controller;

import com.clinic.order.common.security.RequireRole;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PageController {
    @GetMapping("/")
    public String homePage() { return "home"; }

    @GetMapping({"/schedule", "/department/{id}"})
    public String departmentPage(@PathVariable(required = false) Long id, Model model) {
        Long departmentId = id == null ? 1 : id;
        model.addAttribute("departmentId", departmentId);
        model.addAttribute("departmentName", switch (departmentId.intValue()) {
            case 2 -> "儿科";
            case 3 -> "口腔科";
            default -> "内科";
        });
        return "department";
    }

    @GetMapping("/login")
    public String loginPage() { return "login"; }

    @RequireRole(1)
    @GetMapping("/user/dashboard")
    public String userDashboard() { return "user-dashboard"; }

    @GetMapping("/doctor")
    public String doctorHome() { return "doctor-home"; }


    @GetMapping("/login/doctor")
    public String doctorLogin() { return "login"; }

    @RequireRole(2)
    @GetMapping("/doctor/workspace")
    public String doctorWorkspace() { return "doctor-workspace"; }

    @GetMapping("/admin/login")
    public String adminLogin() { return "admin-login"; }

    @RequireRole(3)
    @GetMapping({"/admin/dashboard", "/admin/schedule/manage", "/admin/doctor/manage", "/admin/finance"})
    public String adminDashboard() { return "admin-dashboard"; }
}
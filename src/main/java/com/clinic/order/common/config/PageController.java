package com.clinic.order.common.config;

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
        model.addAttribute("departmentId", id == null ? 1 : id);
        return "department";
    }

    @GetMapping("/user/dashboard")
    public String userDashboard() { return "user-dashboard"; }

    @GetMapping({"/doctor/workspace", "/login/doctor"})
    public String doctorWorkspace() { return "doctor-workspace"; }

    @GetMapping({"/admin/dashboard", "/admin/login", "/admin/schedule/manage"})
    public String adminDashboard() { return "admin-dashboard"; }
}
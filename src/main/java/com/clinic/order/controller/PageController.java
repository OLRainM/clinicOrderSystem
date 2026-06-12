package com.clinic.order.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    @GetMapping({"/", "/schedule"})
    public String schedulePage() {
        return "schedule";
    }
}

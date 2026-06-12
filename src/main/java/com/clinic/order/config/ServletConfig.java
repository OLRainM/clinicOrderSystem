package com.clinic.order.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class ServletConfig {

    @Bean
    public ServletRegistrationBean<HttpServlet> legacyHealthServlet() {
        HttpServlet servlet = new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write("{\"status\":\"UP\",\"module\":\"clinic-order-servlet\"}");
            }
        };
        return new ServletRegistrationBean<>(servlet, "/servlet/health");
    }
}

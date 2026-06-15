package com.clinic.order.common.config;

import com.clinic.order.common.security.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final AuthInterceptor authInterceptor;

    public WebConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**", "/user/**", "/doctor/**", "/admin/**")
                .excludePathPatterns("/api/auth/**", "/api/schedules", "/api/init/**",
                        "/login", "/admin/login", "/login/doctor", "/", "/department/**", "/schedule");
    }
}

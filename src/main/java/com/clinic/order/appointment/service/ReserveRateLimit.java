package com.clinic.order.appointment.service;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ReserveRateLimit {
    int seconds() default 10;
    int maxRequests() default 5;
}

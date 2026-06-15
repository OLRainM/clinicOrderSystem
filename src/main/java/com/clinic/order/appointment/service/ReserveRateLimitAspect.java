package com.clinic.order.appointment.service;

import com.clinic.order.common.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Aspect
@Component
public class ReserveRateLimitAspect {
    private final StringRedisTemplate redisTemplate;

    public ReserveRateLimitAspect(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Around("@annotation(limit)")
    public Object around(ProceedingJoinPoint point, ReserveRateLimit limit) throws Throwable {
        Long userId = findUserId(point.getArgs());
        String key = "hospital:rate:reserve:" + userId;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(limit.seconds()));
        }
        if (count != null && count > limit.maxRequests()) {
            throw new IllegalStateException("预约请求过于频繁，请稍后再试");
        }
        return point.proceed();
    }

    private Long findUserId(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof HttpServletRequest request) {
                return SecurityUtils.currentUserId(request);
            }
        }
        throw new IllegalStateException("限流接口缺少HttpServletRequest参数");
    }
}

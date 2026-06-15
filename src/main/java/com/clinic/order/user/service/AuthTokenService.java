package com.clinic.order.user.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthTokenService {
    public static final String COOKIE_NAME = "CLINIC_TOKEN";
    private static final Duration TTL = Duration.ofHours(2);
    private final StringRedisTemplate redisTemplate;

    public AuthTokenService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String issueToken(Long userId, HttpServletResponse response) {
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(key(token), String.valueOf(userId), TTL);
        Cookie cookie = new Cookie(COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) TTL.toSeconds());
        response.addCookie(cookie);
        return token;
    }

    public Optional<Long> verify(HttpServletRequest request) {
        String token = readToken(request).orElse(null);
        if (token == null) return Optional.empty();
        String userId = redisTemplate.opsForValue().get(key(token));
        if (userId == null) return Optional.empty();
        redisTemplate.expire(key(token), TTL);
        return Optional.of(Long.valueOf(userId));
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        readToken(request).ifPresent(token -> redisTemplate.delete(key(token)));
        Cookie cookie = new Cookie(COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private Optional<String> readToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return Optional.empty();
        return Arrays.stream(cookies).filter(c -> COOKIE_NAME.equals(c.getName())).map(Cookie::getValue).findFirst();
    }

    private String key(String token) { return "auth:token:" + token; }
}

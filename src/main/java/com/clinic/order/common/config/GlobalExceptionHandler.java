package com.clinic.order.common.config;

import com.clinic.order.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> illegalState(IllegalStateException e) {
        return ApiResponse.fail(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> validation(MethodArgumentNotValidException e) {
        return ApiResponse.fail("请求参数不合法");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> system(Exception e) {
        return ApiResponse.fail("系统繁忙，请稍后再试");
    }
}

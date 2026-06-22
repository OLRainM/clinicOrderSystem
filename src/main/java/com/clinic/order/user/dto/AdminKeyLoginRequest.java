package com.clinic.order.user.dto;

import jakarta.validation.constraints.NotBlank;

public class AdminKeyLoginRequest {
    @NotBlank
    private String secretKey;

    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
}

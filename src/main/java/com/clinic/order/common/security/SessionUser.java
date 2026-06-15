package com.clinic.order.common.security;

public class SessionUser {
    private Long userId;
    private String username;
    private Integer roleType;

    public SessionUser() {}

    public SessionUser(Long userId, String username, Integer roleType) {
        this.userId = userId;
        this.username = username;
        this.roleType = roleType;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Integer getRoleType() { return roleType; }
    public void setRoleType(Integer roleType) { this.roleType = roleType; }
}
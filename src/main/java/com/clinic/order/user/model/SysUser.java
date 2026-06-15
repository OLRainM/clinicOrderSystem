package com.clinic.order.user.model;

public class SysUser {
    private Long id;
    private String phone;
    private String passwordHash;
    private Integer roleType;
    private Integer status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Integer getRoleType() { return roleType; }
    public void setRoleType(Integer roleType) { this.roleType = roleType; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}

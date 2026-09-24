package com.billing.usagebilling.dto;

public class UpdateRoleRequest {
    private String username;
    private String role;

    public UpdateRoleRequest() {}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
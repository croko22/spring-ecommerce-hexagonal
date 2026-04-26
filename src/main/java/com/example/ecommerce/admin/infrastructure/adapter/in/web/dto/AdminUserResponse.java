package com.example.ecommerce.admin.infrastructure.adapter.in.web.dto;

import com.example.ecommerce.user.domain.model.User;
import com.example.ecommerce.user.domain.model.UserRole;

public class AdminUserResponse {

    private Long id;
    private String email;
    private UserRole role;

    public AdminUserResponse() {
    }

    public static AdminUserResponse fromDomain(User user) {
        AdminUserResponse response = new AdminUserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}

package com.example.ecommerce.user.infrastructure.adapter.in.web;

import java.security.Principal;

public class UserPrincipal implements Principal {
    private final Long userId;
    private final String email;
    private final String role;

    public UserPrincipal(Long userId, String email) {
        this(userId, email, "USER");
    }

    public UserPrincipal(Long userId, String email, String role) {
        this.userId = userId;
        this.email = email;
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    @Override
    public String getName() {
        return email;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}

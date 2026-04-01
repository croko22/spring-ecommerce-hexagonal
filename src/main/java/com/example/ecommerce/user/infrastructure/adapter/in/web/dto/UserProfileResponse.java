package com.example.ecommerce.user.infrastructure.adapter.in.web.dto;

public class UserProfileResponse {

    private Long id;
    private String username;
    private String email;
    private String role;

    public UserProfileResponse(Long id, String username, String email, String role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}

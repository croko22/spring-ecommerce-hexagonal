package com.example.ecommerce.user.domain.model;

import com.example.ecommerce.user.domain.exception.InvalidUserException;

public class User {
    private Long id;
    private String email;
    private String password;

    public User(Long id, String email, String password) {
        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            throw new InvalidUserException("Invalid email format");
        }
        if (password == null || password.trim().isEmpty() || password.length() < 6) {
            throw new InvalidUserException("Password must be at least 6 characters long");
        }
        this.id = id;
        this.email = email;
        this.password = password;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
}
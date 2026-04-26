package com.example.ecommerce.user.domain.model;

import com.example.ecommerce.user.domain.exception.InvalidUserException;

public class User {
    private Long id;
    private String email;
    private String password;
    private UserRole role;

    public User(Long id, String email, String password) {
        this(id, email, password, UserRole.USER);
    }

    public User(Long id, String email, String password, UserRole role) {
        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            throw new InvalidUserException("Invalid email format");
        }
        if (password == null || password.trim().isEmpty() || password.length() < 6) {
            throw new InvalidUserException("Password must be at least 6 characters long");
        }
        this.id = id;
        this.email = email;
        this.password = password;
        this.role = role != null ? role : UserRole.USER;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
}
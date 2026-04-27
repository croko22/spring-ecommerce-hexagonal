package com.example.ecommerce.user.domain.model;

import com.example.ecommerce.user.domain.exception.InvalidUserException;

import java.time.LocalDateTime;

public class User {
    private Long id;
    private String email;
    private String password;
    private UserRole role;
    private String resetToken;
    private LocalDateTime resetTokenExpiry;

    public User(Long id, String email, String password) {
        this(id, email, password, UserRole.USER, null, null);
    }

    public User(Long id, String email, String password, UserRole role) {
        this(id, email, password, role, null, null);
    }

    public User(Long id, String email, String password, UserRole role,
                String resetToken, LocalDateTime resetTokenExpiry) {
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
        this.resetToken = resetToken;
        this.resetTokenExpiry = resetTokenExpiry;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }

    public LocalDateTime getResetTokenExpiry() { return resetTokenExpiry; }
    public void setResetTokenExpiry(LocalDateTime resetTokenExpiry) { this.resetTokenExpiry = resetTokenExpiry; }

    public boolean isResetTokenValid(String token) {
        return this.resetToken != null
                && this.resetToken.equals(token)
                && this.resetTokenExpiry != null
                && this.resetTokenExpiry.isAfter(LocalDateTime.now());
    }
}
package com.example.ecommerce.user.application.port.in;

public interface AuthenticateUserUseCase {
    record TokenPair(String accessToken, String refreshToken, long expiresIn) {}
    TokenPair authenticate(String email, String rawPassword);
}
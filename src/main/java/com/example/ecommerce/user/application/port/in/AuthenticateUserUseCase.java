package com.example.ecommerce.user.application.port.in;

public interface AuthenticateUserUseCase {
    String authenticate(String email, String rawPassword);
}
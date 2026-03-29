package com.example.ecommerce.user.application.port.in;

public interface GenerateTokenUseCase {
    String generateToken(String email, Long userId);
}

package com.example.ecommerce.user.application.port.in;

public interface RefreshTokenUseCase {
    String refreshToken(String refreshToken);
    String generateRefreshToken(String email, Long userId);
}

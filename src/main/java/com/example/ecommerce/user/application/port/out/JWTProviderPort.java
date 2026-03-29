package com.example.ecommerce.user.application.port.out;

import com.example.ecommerce.user.application.port.in.GenerateTokenUseCase;
import com.example.ecommerce.user.application.port.in.RefreshTokenUseCase;
import com.example.ecommerce.user.application.port.in.ValidateTokenUseCase;

public interface JWTProviderPort extends GenerateTokenUseCase, ValidateTokenUseCase, RefreshTokenUseCase {
    String generateRefreshToken(String email, Long userId);
    boolean validateRefreshToken(String refreshToken);
}

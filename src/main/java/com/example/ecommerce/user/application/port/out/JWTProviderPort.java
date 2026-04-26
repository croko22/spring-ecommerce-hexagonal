package com.example.ecommerce.user.application.port.out;

public interface JWTProviderPort {
    String generateToken(String email, Long userId, String role);
    String generateRefreshToken(String email, Long userId, String role);
    boolean validateToken(String token);
    String getEmailFromToken(String token);
    Long getUserIdFromToken(String token);
    String getRoleFromToken(String token);
    String refreshToken(String refreshToken);
    boolean validateRefreshToken(String refreshToken);
}

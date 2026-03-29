package com.example.ecommerce.user.application.port.in;

public interface ValidateTokenUseCase {
    boolean validateToken(String token);
    String getEmailFromToken(String token);
    Long getUserIdFromToken(String token);
}

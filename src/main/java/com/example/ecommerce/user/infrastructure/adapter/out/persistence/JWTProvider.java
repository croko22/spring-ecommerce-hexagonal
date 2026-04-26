package com.example.ecommerce.user.infrastructure.adapter.out.persistence;

import com.example.ecommerce.user.application.port.out.JWTProviderPort;
import com.example.ecommerce.user.application.service.JWTService;
import org.springframework.stereotype.Component;

@Component
public class JWTProvider implements JWTProviderPort {

    private final JWTService jwtService;

    public JWTProvider(JWTService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public String generateToken(String email, Long userId, String role) {
        return jwtService.generateToken(email, userId, role);
    }

    @Override
    public String generateRefreshToken(String email, Long userId, String role) {
        return jwtService.generateRefreshToken(email, userId, role);
    }

    @Override
    public boolean validateToken(String token) {
        return jwtService.validateToken(token);
    }

    @Override
    public String getEmailFromToken(String token) {
        return jwtService.getEmailFromToken(token);
    }

    @Override
    public Long getUserIdFromToken(String token) {
        return jwtService.getUserIdFromToken(token);
    }

    @Override
    public String getRoleFromToken(String token) {
        return jwtService.getRoleFromToken(token);
    }

    @Override
    public String refreshToken(String refreshToken) {
        return jwtService.refreshToken(refreshToken);
    }

    @Override
    public boolean validateRefreshToken(String refreshToken) {
        return jwtService.validateRefreshToken(refreshToken);
    }
}

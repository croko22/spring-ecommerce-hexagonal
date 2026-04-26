package com.example.ecommerce.user.application.service;

import com.example.ecommerce.user.domain.exception.InvalidTokenException;
import com.example.ecommerce.user.domain.exception.TokenExpiredException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JWTServiceTest {

    private JWTService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JWTService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", 
                "mySecretKeyForJWTTokenGenerationThatIsLongEnoughForHS256Algorithm123456");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400000L);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", 604800000L);
    }

    @Test
    void shouldGenerateTokenSuccessfully() {
        String email = "test@example.com";
        Long userId = 1L;

        String token = jwtService.generateToken(email, userId, "USER");

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void shouldGenerateRefreshTokenSuccessfully() {
        String email = "test@example.com";
        Long userId = 1L;

        String refreshToken = jwtService.generateRefreshToken(email, userId, "USER");

        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());
    }

    @Test
    void shouldValidateValidToken() {
        String email = "test@example.com";
        Long userId = 1L;
        String token = jwtService.generateToken(email, userId, "USER");

        boolean isValid = jwtService.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    void shouldInvalidateInvalidToken() {
        String invalidToken = "invalid.token.here";

        boolean isValid = jwtService.validateToken(invalidToken);

        assertFalse(isValid);
    }

    @Test
    void shouldExtractEmailFromToken() {
        String email = "test@example.com";
        Long userId = 1L;
        String token = jwtService.generateToken(email, userId, "USER");

        String extractedEmail = jwtService.getEmailFromToken(token);

        assertEquals(email, extractedEmail);
    }

    @Test
    void shouldExtractUserIdFromToken() {
        String email = "test@example.com";
        Long userId = 1L;
        String token = jwtService.generateToken(email, userId, "USER");

        Long extractedUserId = jwtService.getUserIdFromToken(token);

        assertEquals(userId, extractedUserId);
    }

    @Test
    void shouldValidateRefreshToken() {
        String email = "test@example.com";
        Long userId = 1L;
        String refreshToken = jwtService.generateRefreshToken(email, userId, "USER");

        boolean isValid = jwtService.validateRefreshToken(refreshToken);

        assertTrue(isValid);
    }

    @Test
    void shouldInvalidateAccessTokenAsRefreshToken() {
        String email = "test@example.com";
        Long userId = 1L;
        String accessToken = jwtService.generateToken(email, userId, "USER");

        boolean isValid = jwtService.validateRefreshToken(accessToken);

        assertFalse(isValid);
    }

    @Test
    void shouldRefreshTokenSuccessfully() {
        String email = "test@example.com";
        Long userId = 1L;
        String refreshToken = jwtService.generateRefreshToken(email, userId, "USER");

        String newAccessToken = jwtService.refreshToken(refreshToken);

        assertNotNull(newAccessToken);
        assertTrue(jwtService.validateToken(newAccessToken));
    }

    @Test
    void shouldThrowExceptionWhenRefreshingInvalidToken() {
        String invalidToken = "invalid.refresh.token";

        assertThrows(InvalidTokenException.class, () -> {
            jwtService.refreshToken(invalidToken);
        });
    }
}

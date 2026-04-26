package com.example.ecommerce.user.application.service;

import com.example.ecommerce.user.application.port.in.AuthenticateUserUseCase;
import com.example.ecommerce.user.application.port.out.JWTProviderPort;
import com.example.ecommerce.user.application.port.out.PasswordEncoderPort;
import com.example.ecommerce.user.application.port.out.UserRepositoryPort;
import com.example.ecommerce.user.domain.exception.AuthenticationException;
import com.example.ecommerce.user.domain.exception.UserAlreadyExistsException;
import com.example.ecommerce.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;

    @Mock
    private JWTProviderPort jwtProviderPort;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepositoryPort, passwordEncoderPort, jwtProviderPort);
        ReflectionTestUtils.setField(userService, "jwtExpiration", 86400000L);
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        // Arrange
        String email = "test@example.com";
        String rawPassword = "password123";
        String encodedPassword = "encodedPassword";
        User savedUser = new User(1L, email, encodedPassword);

        when(userRepositoryPort.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoderPort.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepositoryPort.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = userService.register(email, rawPassword);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(email, result.getEmail());
        assertEquals(encodedPassword, result.getPassword());
        
        verify(userRepositoryPort).findByEmail(email);
        verify(passwordEncoderPort).encode(rawPassword);
        verify(userRepositoryPort).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenRegisteringExistingUser() {
        // Arrange
        String email = "test@example.com";
        String rawPassword = "password123";
        User existingUser = new User(1L, email, "encodedPassword");

        when(userRepositoryPort.findByEmail(email)).thenReturn(Optional.of(existingUser));

        // Act & Assert
        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () -> {
            userService.register(email, rawPassword);
        });
        assertEquals("User with email " + email + " already exists", exception.getMessage());
        
        verify(userRepositoryPort).findByEmail(email);
        verify(userRepositoryPort, never()).save(any(User.class));
    }

    @Test
    void shouldAuthenticateUserSuccessfully() {
        // Arrange
        String email = "test@example.com";
        String rawPassword = "password123";
        String encodedPassword = "encodedPassword";
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";
        User user = new User(1L, email, encodedPassword);

        when(userRepositoryPort.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoderPort.matches(rawPassword, encodedPassword)).thenReturn(true);
        when(jwtProviderPort.generateToken(email, 1L, "USER")).thenReturn(accessToken);
        when(jwtProviderPort.generateRefreshToken(email, 1L, "USER")).thenReturn(refreshToken);

        // Act
        AuthenticateUserUseCase.TokenPair tokenPair = userService.authenticate(email, rawPassword);

        // Assert
        assertNotNull(tokenPair);
        assertEquals(accessToken, tokenPair.accessToken());
        assertEquals(refreshToken, tokenPair.refreshToken());
        
        verify(userRepositoryPort).findByEmail(email);
        verify(passwordEncoderPort).matches(rawPassword, encodedPassword);
    }

    @Test
    void shouldThrowExceptionWhenAuthenticatingWithWrongPassword() {
        // Arrange
        String email = "test@example.com";
        String rawPassword = "wrongPassword";
        String encodedPassword = "encodedPassword";
        User user = new User(1L, email, encodedPassword);

        when(userRepositoryPort.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoderPort.matches(rawPassword, encodedPassword)).thenReturn(false);

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            userService.authenticate(email, rawPassword);
        });
        assertEquals("Invalid email or password", exception.getMessage());
        
        verify(userRepositoryPort).findByEmail(email);
        verify(passwordEncoderPort).matches(rawPassword, encodedPassword);
    }
}
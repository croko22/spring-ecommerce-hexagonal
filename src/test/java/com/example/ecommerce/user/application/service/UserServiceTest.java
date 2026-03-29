package com.example.ecommerce.user.application.service;

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

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepositoryPort, passwordEncoderPort);
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
        User user = new User(1L, email, encodedPassword);

        when(userRepositoryPort.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoderPort.matches(rawPassword, encodedPassword)).thenReturn(true);

        // Act
        String token = userService.authenticate(email, rawPassword);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        
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
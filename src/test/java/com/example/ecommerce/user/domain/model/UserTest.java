package com.example.ecommerce.user.domain.model;

import com.example.ecommerce.user.domain.exception.InvalidUserException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void shouldCreateUserWithValidData() {
        // Arrange & Act
        User user = new User(1L, "test@example.com", "password123");

        // Assert
        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
    }

    @Test
    void shouldThrowExceptionWhenEmailIsInvalid() {
        // Arrange & Act & Assert
        InvalidUserException exception = assertThrows(InvalidUserException.class, () -> {
            new User(1L, "invalid-email", "password123");
        });
        assertEquals("Invalid email format", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsTooShort() {
        // Arrange & Act & Assert
        InvalidUserException exception = assertThrows(InvalidUserException.class, () -> {
            new User(1L, "test@example.com", "pass");
        });
        assertEquals("Password must be at least 6 characters long", exception.getMessage());
    }
}
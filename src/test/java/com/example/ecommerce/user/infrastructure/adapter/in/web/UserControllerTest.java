package com.example.ecommerce.user.infrastructure.adapter.in.web;

import com.example.ecommerce.user.application.port.in.AuthenticateUserUseCase;
import com.example.ecommerce.user.application.port.in.RegisterUserUseCase;
import com.example.ecommerce.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RegisterUserUseCase registerUserUseCase;

    @Mock
    private AuthenticateUserUseCase authenticateUserUseCase;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new UserExceptionHandler())
                .build();
    }

    @Test
    void shouldRegisterUser() throws Exception {
        // Arrange
        String email = "test@example.com";
        String password = "password123";
        User user = new User(1L, email, "encoded");

        when(registerUserUseCase.register(anyString(), anyString())).thenReturn(user);

        String requestBody = "{\"email\":\"" + email + "\", \"password\":\"" + password + "\"}";

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void shouldLoginUser() throws Exception {
        // Arrange
        String email = "test@example.com";
        String password = "password123";
        String token = "dummy-token";

        when(authenticateUserUseCase.authenticate(anyString(), anyString())).thenReturn(token);

        String requestBody = "{\"email\":\"" + email + "\", \"password\":\"" + password + "\"}";

        // Act & Assert
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(token));
    }
}
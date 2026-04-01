package com.example.ecommerce.user.infrastructure.adapter.in.web;

import com.example.ecommerce.user.application.port.out.JWTProviderPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserMeEndpointSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JWTProviderPort jwtProviderPort;

    @Test
    void shouldReturnUnauthorizedWhenTokenIsMissing() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenIsInvalid() throws Exception {
        when(jwtProviderPort.validateToken("invalid-token")).thenReturn(false);

        mockMvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnAuthenticatedUserProfileForValidToken() throws Exception {
        when(jwtProviderPort.validateToken("valid-token")).thenReturn(true);
        when(jwtProviderPort.getEmailFromToken("valid-token")).thenReturn("john.doe@example.com");
        when(jwtProviderPort.getUserIdFromToken("valid-token")).thenReturn(42L);

        mockMvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.username").value("john.doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void shouldReturnAuthenticatedUserProfileOnApiAliasForValidToken() throws Exception {
        when(jwtProviderPort.validateToken("valid-token")).thenReturn(true);
        when(jwtProviderPort.getEmailFromToken("valid-token")).thenReturn("john.doe@example.com");
        when(jwtProviderPort.getUserIdFromToken("valid-token")).thenReturn(42L);

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.username").value("john.doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }
}

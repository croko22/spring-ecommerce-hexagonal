package com.example.ecommerce.payment.infrastructure.adapter.in.web;

import com.example.ecommerce.payment.application.port.in.GetPaymentUseCase;
import com.example.ecommerce.payment.application.port.in.HandlePaymentWebhookUseCase;
import com.example.ecommerce.payment.application.port.in.InitiatePaymentUseCase;
import com.example.ecommerce.user.application.port.out.JWTProviderPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentEndpointSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JWTProviderPort jwtProviderPort;

    @MockitoBean
    private InitiatePaymentUseCase initiatePaymentUseCase;

    @MockitoBean
    private GetPaymentUseCase getPaymentUseCase;

    @MockitoBean
    private HandlePaymentWebhookUseCase handlePaymentWebhookUseCase;

    @Test
    void shouldReturnUnauthorizedWhenTokenIsMissing() throws Exception {
        mockMvc.perform(post("/api/payments")
                        .contentType("application/json")
                        .content("{" +
                                "\"orderId\":10," +
                                "\"idempotencyKey\":\"idem-1\"," +
                                "\"paymentMethodToken\":\"pm-ok\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenIsInvalid() throws Exception {
        when(jwtProviderPort.validateToken("invalid-token")).thenReturn(false);

        mockMvc.perform(get("/api/payments/1")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAuthenticatedPaymentInitiationAndIgnoreSpoofedHeader() throws Exception {
        when(jwtProviderPort.validateToken("valid-token")).thenReturn(true);
        when(jwtProviderPort.getEmailFromToken("valid-token")).thenReturn("john.doe@example.com");
        when(jwtProviderPort.getUserIdFromToken("valid-token")).thenReturn(42L);
        when(initiatePaymentUseCase.initiatePayment(any())).thenReturn(
                new InitiatePaymentUseCase.InitiatePaymentResult(
                        501L,
                        "CAPTURED",
                        new BigDecimal("50.00"),
                        "USD",
                        77L,
                        "prov-501"
                )
        );

        mockMvc.perform(post("/api/payments")
                        .header("Authorization", "Bearer valid-token")
                        .header("X-User-Id", "999")
                        .contentType("application/json")
                        .content("{" +
                                "\"orderId\":77," +
                                "\"idempotencyKey\":\"idem-2\"," +
                                "\"paymentMethodToken\":\"pm-ok\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentId").value(501))
                .andExpect(jsonPath("$.status").value("CAPTURED"))
                .andExpect(jsonPath("$.orderId").value(77));
    }

    @Test
    void shouldAllowWebhookWithoutAuthentication() throws Exception {
        when(handlePaymentWebhookUseCase.handleWebhook(any())).thenReturn(
                new HandlePaymentWebhookUseCase.HandlePaymentWebhookResult(700L, "evt-700", "ACCEPTED", "NO_OP")
        );

        mockMvc.perform(post("/api/payments/webhooks/provider")
                        .header("X-Provider-Event-Id", "evt-700")
                        .header("X-Provider-Signature", "sig-valid")
                        .contentType("application/json")
                        .content("{\"type\":\"payment.captured\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.receiptId").value(700));
    }
}

package com.example.ecommerce.payment.infrastructure.adapter.in.web;

import com.example.ecommerce.payment.application.exception.IdempotencyConflictException;
import com.example.ecommerce.payment.application.exception.IdempotencyInProgressException;
import com.example.ecommerce.payment.application.exception.PaymentAccessDeniedException;
import com.example.ecommerce.payment.application.exception.PaymentWebhookSignatureInvalidException;
import com.example.ecommerce.payment.application.exception.ProviderTimeoutException;
import com.example.ecommerce.payment.application.port.in.GetPaymentUseCase;
import com.example.ecommerce.payment.application.port.in.HandlePaymentWebhookUseCase;
import com.example.ecommerce.payment.application.port.in.InitiatePaymentUseCase;
import com.example.ecommerce.payment.domain.exception.InvalidPaymentTransitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private InitiatePaymentUseCase initiatePaymentUseCase;

    @Mock
    private GetPaymentUseCase getPaymentUseCase;

    @Mock
    private HandlePaymentWebhookUseCase handlePaymentWebhookUseCase;

    @BeforeEach
    void setUp() {
        PaymentController paymentController = new PaymentController(initiatePaymentUseCase, getPaymentUseCase);
        PaymentWebhookController paymentWebhookController = new PaymentWebhookController(handlePaymentWebhookUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController, paymentWebhookController)
                .setControllerAdvice(new PaymentExceptionHandler())
                .build();
    }

    @Test
    void shouldInitiatePaymentAndReturn201IgnoringSpoofedHeader() throws Exception {
        String requestBody = """
                {
                  "orderId": 10,
                  "idempotencyKey": "idem-1",
                  "paymentMethodToken": "pm-ok"
                }
                """;

        when(initiatePaymentUseCase.initiatePayment(any())).thenReturn(
                new InitiatePaymentUseCase.InitiatePaymentResult(
                        101L,
                        "CAPTURED",
                        new BigDecimal("10.24"),
                        "USD",
                        10L,
                        "prov-101"
                )
        );

        mockMvc.perform(post("/api/payments")
                        .header("X-User-Id", "999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentId").value(101))
                .andExpect(jsonPath("$.status").value("CAPTURED"))
                .andExpect(jsonPath("$.amount").value(10.24))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.orderId").value(10))
                .andExpect(jsonPath("$.providerReference").value("prov-101"));
    }

    @Test
    void shouldGetPaymentDetailsAndReturn200() throws Exception {
        when(getPaymentUseCase.getPaymentById(42L)).thenReturn(
                new GetPaymentUseCase.PaymentDetails(
                        42L,
                        9L,
                        7L,
                        "CAPTURED",
                        new BigDecimal("20.00"),
                        "USD",
                        "prov-42",
                        null
                )
        );

        mockMvc.perform(get("/api/payments/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(42))
                .andExpect(jsonPath("$.status").value("CAPTURED"))
                .andExpect(jsonPath("$.amount").value(20.00))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.orderId").value(9))
                .andExpect(jsonPath("$.providerReference").value("prov-42"));
    }

    @Test
    void shouldMapDomainTransitionErrorTo400() throws Exception {
        when(initiatePaymentUseCase.initiatePayment(any()))
                .thenThrow(new InvalidPaymentTransitionException("Invalid transition"));

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"orderId\":10," +
                                "\"idempotencyKey\":\"idem-x\"," +
                                "\"paymentMethodToken\":\"pm-x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PAYMENT_DOMAIN_ERROR"))
                .andExpect(jsonPath("$.message").value("Invalid transition"))
                .andExpect(jsonPath("$.retryable").value(false))
                .andExpect(jsonPath("$.path").value("/api/payments"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void shouldMapIdempotencyConflictTo409() throws Exception {
        when(initiatePaymentUseCase.initiatePayment(any()))
                .thenThrow(new IdempotencyConflictException("Idempotency conflict"));

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"orderId\":10," +
                                "\"idempotencyKey\":\"idem-x\"," +
                                "\"paymentMethodToken\":\"pm-x\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PAYMENT_IDEMPOTENCY_CONFLICT"))
                .andExpect(jsonPath("$.retryable").value(false));
    }

    @Test
    void shouldMapIdempotencyInProgressTo409Retryable() throws Exception {
        when(initiatePaymentUseCase.initiatePayment(any()))
                .thenThrow(new IdempotencyInProgressException("Payment request is already in progress for key idem-x"));

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"orderId\":10," +
                                "\"idempotencyKey\":\"idem-x\"," +
                                "\"paymentMethodToken\":\"pm-x\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PAYMENT_IN_PROGRESS"))
                .andExpect(jsonPath("$.retryable").value(true));
    }

    @Test
    void shouldMapAccessDeniedTo403WithoutOwnershipDetails() throws Exception {
        when(initiatePaymentUseCase.initiatePayment(any()))
                .thenThrow(new PaymentAccessDeniedException("Access denied to payment resource"));

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"orderId\":10," +
                                "\"idempotencyKey\":\"idem-x\"," +
                                "\"paymentMethodToken\":\"pm-x\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PAYMENT_FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("Access denied to payment resource"))
                .andExpect(jsonPath("$.retryable").value(false));
    }

    @Test
    void shouldMapProviderTimeoutTo503Retryable() throws Exception {
        when(initiatePaymentUseCase.initiatePayment(any()))
                .thenThrow(new ProviderTimeoutException("Provider timeout"));

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"orderId\":10," +
                                "\"idempotencyKey\":\"idem-x\"," +
                                "\"paymentMethodToken\":\"pm-x\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("PAYMENT_PROVIDER_TIMEOUT"))
                .andExpect(jsonPath("$.message").value("Provider timeout"))
                .andExpect(jsonPath("$.retryable").value(true))
                .andExpect(jsonPath("$.path").value("/api/payments"));
    }

    @Test
    void shouldAcceptValidWebhookAndPersistReceipt() throws Exception {
        String payload = "{\"type\":\"payment.captured\"}";

        when(handlePaymentWebhookUseCase.handleWebhook(any()))
                .thenReturn(new HandlePaymentWebhookUseCase.HandlePaymentWebhookResult(901L, "evt-901", "ACCEPTED", "NO_OP"));

        mockMvc.perform(post("/api/payments/webhooks/provider")
                        .header("X-Provider-Event-Id", "evt-901")
                        .header("X-Provider-Signature", "sig-valid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.receiptId").value(901))
                .andExpect(jsonPath("$.providerEventId").value("evt-901"))
                .andExpect(jsonPath("$.ingestStatus").value("ACCEPTED"))
                .andExpect(jsonPath("$.reconciliationStatus").value("NO_OP"));
    }

    @Test
    void shouldRejectWebhookWhenSignatureIsInvalid() throws Exception {
        when(handlePaymentWebhookUseCase.handleWebhook(any()))
                .thenThrow(new PaymentWebhookSignatureInvalidException("Invalid payment webhook signature"));

        mockMvc.perform(post("/api/payments/webhooks/provider")
                        .header("X-Provider-Event-Id", "evt-902")
                        .header("X-Provider-Signature", "bad")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"payment.captured\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("PAYMENT_WEBHOOK_SIGNATURE_INVALID"))
                .andExpect(jsonPath("$.retryable").value(false));
    }
}

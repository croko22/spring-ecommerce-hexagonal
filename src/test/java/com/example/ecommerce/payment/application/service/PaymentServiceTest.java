package com.example.ecommerce.payment.application.service;

import com.example.ecommerce.payment.application.exception.IdempotencyConflictException;
import com.example.ecommerce.payment.application.exception.IdempotencyInProgressException;
import com.example.ecommerce.payment.application.exception.PaymentAccessDeniedException;
import com.example.ecommerce.payment.application.exception.PaymentWebhookSignatureInvalidException;
import com.example.ecommerce.payment.application.port.in.HandlePaymentWebhookUseCase;
import com.example.ecommerce.payment.application.port.in.InitiatePaymentUseCase;
import com.example.ecommerce.payment.application.port.out.OrderPaymentTransitionPort;
import com.example.ecommerce.payment.application.port.out.OrderSnapshotPort;
import com.example.ecommerce.payment.application.port.out.PaymentIdempotencyPort;
import com.example.ecommerce.payment.application.port.out.PaymentProviderPort;
import com.example.ecommerce.payment.application.port.out.PaymentRepositoryPort;
import com.example.ecommerce.payment.application.port.out.PaymentWebhookReceiptPort;
import com.example.ecommerce.payment.application.port.out.PaymentWebhookSignaturePort;
import com.example.ecommerce.payment.application.port.out.PrincipalAccessPort;
import com.example.ecommerce.payment.domain.model.CurrencyCode;
import com.example.ecommerce.payment.domain.model.IdempotencyKey;
import com.example.ecommerce.payment.domain.model.Money;
import com.example.ecommerce.payment.domain.model.Payment;
import com.example.ecommerce.payment.domain.model.PaymentOperation;
import com.example.ecommerce.payment.domain.model.PaymentStatus;
import com.example.ecommerce.payment.domain.model.ProviderReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PrincipalAccessPort principalAccessPort;

    @Mock
    private OrderSnapshotPort orderSnapshotPort;

    @Mock
    private PaymentIdempotencyPort paymentIdempotencyPort;

    @Mock
    private PaymentRepositoryPort paymentRepositoryPort;

    @Mock
    private PaymentProviderPort paymentProviderPort;

    @Mock
    private OrderPaymentTransitionPort orderPaymentTransitionPort;

    @Mock
    private PaymentWebhookSignaturePort paymentWebhookSignaturePort;

    @Mock
    private PaymentWebhookReceiptPort paymentWebhookReceiptPort;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(
                principalAccessPort,
                orderSnapshotPort,
                paymentIdempotencyPort,
                paymentRepositoryPort,
                paymentProviderPort,
                orderPaymentTransitionPort,
                paymentWebhookSignaturePort,
                paymentWebhookReceiptPort
        );
    }

    @Test
    void shouldCaptureAndMarkOrderPaidWhenProviderSucceeds() {
        InitiatePaymentUseCase.InitiatePaymentCommand command = new InitiatePaymentUseCase.InitiatePaymentCommand(
                10L,
                "idem-1",
                "pm-ok"
        );

        when(principalAccessPort.getCurrentUserId()).thenReturn(7L);
        when(orderSnapshotPort.getOrderSnapshot(10L)).thenReturn(new OrderSnapshotPort.OrderSnapshot(
                10L,
                7L,
                "PENDING",
                10.235,
                "USD"
        ));
        when(paymentIdempotencyPort.acquireOrReplay(eq(PaymentOperation.INITIATE), eq("7:10"), eq(new IdempotencyKey("idem-1")), anyString()))
                .thenReturn(new PaymentIdempotencyPort.Acquired());
        when(paymentRepositoryPort.save(any(Payment.class)))
                .thenAnswer(invocation -> {
                    Payment payment = invocation.getArgument(0);
                    if (payment.getId() == null) {
                        payment.setId(99L);
                    }
                    return payment;
                });
        when(paymentProviderPort.authorizeAndCapture(10L, 7L, "pm-ok"))
                .thenReturn(new PaymentProviderPort.ProviderAuthorizeCaptureResult(true, "prov-123", null));

        InitiatePaymentUseCase.InitiatePaymentResult result = paymentService.initiatePayment(command);

        assertNotNull(result);
        assertEquals(99L, result.paymentId());
        assertEquals("CAPTURED", result.status());
        assertTrue(result.amount().compareTo(new BigDecimal("10.24")) == 0);
        assertEquals("USD", result.currency());
        assertEquals(10L, result.orderId());
        assertEquals("prov-123", result.providerReference());

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepositoryPort, atLeastOnce()).save(paymentCaptor.capture());
        Payment initiatedPayment = paymentCaptor.getAllValues().get(0);
        assertEquals(new BigDecimal("10.24"), initiatedPayment.getAmount().getAmount());

        verify(orderPaymentTransitionPort).markPaidAfterPayment(10L, 99L);
        verify(paymentIdempotencyPort).complete(
                eq(PaymentOperation.INITIATE),
                eq("7:10"),
                eq(new IdempotencyKey("idem-1")),
                any(String.class),
                eq(99L),
                eq("CAPTURED"),
                any(String.class)
        );
    }

    @Test
    void shouldNotMarkOrderPaidWhenProviderFails() {
        InitiatePaymentUseCase.InitiatePaymentCommand command = new InitiatePaymentUseCase.InitiatePaymentCommand(
                11L,
                "idem-2",
                "pm-fail"
        );

        when(principalAccessPort.getCurrentUserId()).thenReturn(7L);
        when(orderSnapshotPort.getOrderSnapshot(11L)).thenReturn(new OrderSnapshotPort.OrderSnapshot(
                11L,
                7L,
                "PENDING",
                12.0,
                "USD"
        ));
        when(paymentIdempotencyPort.acquireOrReplay(eq(PaymentOperation.INITIATE), eq("7:11"), eq(new IdempotencyKey("idem-2")), anyString()))
                .thenReturn(new PaymentIdempotencyPort.Acquired());
        when(paymentRepositoryPort.save(any(Payment.class)))
                .thenAnswer(invocation -> {
                    Payment payment = invocation.getArgument(0);
                    if (payment.getId() == null) {
                        payment.setId(101L);
                    }
                    return payment;
                });
        when(paymentProviderPort.authorizeAndCapture(11L, 7L, "pm-fail"))
                .thenReturn(new PaymentProviderPort.ProviderAuthorizeCaptureResult(false, null, "DECLINED"));

        InitiatePaymentUseCase.InitiatePaymentResult result = paymentService.initiatePayment(command);

        assertEquals("FAILED", result.status());
        assertTrue(result.amount().compareTo(new BigDecimal("12.00")) == 0);
        assertEquals("USD", result.currency());
        assertEquals(11L, result.orderId());
        verify(orderPaymentTransitionPort, never()).markPaidAfterPayment(any(Long.class), any(Long.class));
    }

    @Test
    void shouldReplayStoredResultForDuplicateRequest() {
        InitiatePaymentUseCase.InitiatePaymentCommand command = new InitiatePaymentUseCase.InitiatePaymentCommand(
                12L,
                "idem-3",
                "pm-ok"
        );

        when(principalAccessPort.getCurrentUserId()).thenReturn(7L);
        when(orderSnapshotPort.getOrderSnapshot(12L)).thenReturn(new OrderSnapshotPort.OrderSnapshot(
                12L,
                7L,
                "PENDING",
                9.0,
                "USD"
        ));
        when(paymentIdempotencyPort.acquireOrReplay(eq(PaymentOperation.INITIATE), eq("7:12"), eq(new IdempotencyKey("idem-3")), anyString()))
                .thenReturn(new PaymentIdempotencyPort.Replay(
                        new PaymentIdempotencyPort.StoredResponse(77L, "CAPTURED", "77|CAPTURED|9.00|USD|12|prov-x", "hash-a")
                ));

        InitiatePaymentUseCase.InitiatePaymentResult result = paymentService.initiatePayment(command);

        assertEquals(77L, result.paymentId());
        assertEquals("CAPTURED", result.status());
        assertTrue(result.amount().compareTo(new BigDecimal("9.00")) == 0);
        assertEquals("USD", result.currency());
        assertEquals(12L, result.orderId());
        assertEquals("prov-x", result.providerReference());
        verify(paymentRepositoryPort, never()).save(any(Payment.class));
        verify(paymentProviderPort, never()).authorizeAndCapture(any(Long.class), any(Long.class), any(String.class));
        verify(orderPaymentTransitionPort, never()).markPaidAfterPayment(any(Long.class), any(Long.class));
    }

    @Test
    void shouldThrowInProgressWhenSameIdempotencyKeyIsActive() {
        InitiatePaymentUseCase.InitiatePaymentCommand command = new InitiatePaymentUseCase.InitiatePaymentCommand(
                120L,
                "idem-in-progress",
                "pm-ok"
        );

        when(principalAccessPort.getCurrentUserId()).thenReturn(7L);
        when(orderSnapshotPort.getOrderSnapshot(120L)).thenReturn(new OrderSnapshotPort.OrderSnapshot(
                120L,
                7L,
                "PENDING",
                9.0,
                "USD"
        ));
        when(paymentIdempotencyPort.acquireOrReplay(eq(PaymentOperation.INITIATE), eq("7:120"), eq(new IdempotencyKey("idem-in-progress")), anyString()))
                .thenReturn(new PaymentIdempotencyPort.InProgress());

        assertThrows(IdempotencyInProgressException.class, () -> paymentService.initiatePayment(command));
        verify(paymentProviderPort, never()).authorizeAndCapture(any(Long.class), any(Long.class), any(String.class));
        verify(paymentRepositoryPort, never()).save(any(Payment.class));
    }

    @Test
    void shouldThrowConflictWhenIdempotencyValidationFails() {
        InitiatePaymentUseCase.InitiatePaymentCommand command = new InitiatePaymentUseCase.InitiatePaymentCommand(
                13L,
                "idem-4",
                "pm-ok"
        );

        when(principalAccessPort.getCurrentUserId()).thenReturn(7L);
        when(orderSnapshotPort.getOrderSnapshot(13L)).thenReturn(new OrderSnapshotPort.OrderSnapshot(
                13L,
                7L,
                "PENDING",
                9.0,
                "USD"
        ));
        doThrow(new IdempotencyConflictException("conflict"))
                .when(paymentIdempotencyPort)
                .acquireOrReplay(eq(PaymentOperation.INITIATE), eq("7:13"), eq(new IdempotencyKey("idem-4")), anyString());

        assertThrows(IdempotencyConflictException.class, () -> paymentService.initiatePayment(command));
        verify(paymentRepositoryPort, never()).save(any(Payment.class));
    }

    @Test
    void shouldDenyInitiatePaymentWhenPrincipalDoesNotOwnOrder() {
        InitiatePaymentUseCase.InitiatePaymentCommand command = new InitiatePaymentUseCase.InitiatePaymentCommand(
                300L,
                "idem-ownership",
                "pm-ok"
        );

        when(principalAccessPort.getCurrentUserId()).thenReturn(7L);
        when(orderSnapshotPort.getOrderSnapshot(300L)).thenReturn(new OrderSnapshotPort.OrderSnapshot(
                300L,
                8L,
                "PENDING",
                20.0,
                "USD"
        ));

        assertThrows(PaymentAccessDeniedException.class, () -> paymentService.initiatePayment(command));
        verify(paymentIdempotencyPort, never()).acquireOrReplay(any(), anyString(), any(), anyString());
        verify(paymentProviderPort, never()).authorizeAndCapture(any(Long.class), any(Long.class), any(String.class));
    }

    @Test
    void shouldReturnPaymentDetailsForOwnedPayment() {
        when(principalAccessPort.getCurrentUserId()).thenReturn(22L);
        Payment payment = new Payment(
                501L,
                55L,
                22L,
                new Money(new BigDecimal("19.99"), new CurrencyCode("USD")),
                PaymentStatus.CAPTURED,
                new ProviderReference("prov-501"),
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(paymentRepositoryPort.findById(501L)).thenReturn(Optional.of(payment));

        var details = paymentService.getPaymentById(501L);

        assertEquals(501L, details.paymentId());
        assertEquals("CAPTURED", details.status());
        assertTrue(details.amount().compareTo(new BigDecimal("19.99")) == 0);
    }

    @Test
    void shouldRejectWebhookWhenSignatureIsInvalid() {
        HandlePaymentWebhookUseCase.HandlePaymentWebhookCommand command =
                new HandlePaymentWebhookUseCase.HandlePaymentWebhookCommand(
                        "evt-1",
                        "sig-invalid",
                        "{\"type\":\"payment.captured\"}"
                );

        when(paymentWebhookSignaturePort.isValidSignature("sig-invalid", "{\"type\":\"payment.captured\"}"))
                .thenReturn(false);

        assertThrows(PaymentWebhookSignatureInvalidException.class, () -> paymentService.handleWebhook(command));
        verify(paymentWebhookReceiptPort, never()).save(any());
    }

    @Test
    void shouldPersistWebhookReceiptAndReturnAcceptedWhenSignatureIsValid() {
        HandlePaymentWebhookUseCase.HandlePaymentWebhookCommand command =
                new HandlePaymentWebhookUseCase.HandlePaymentWebhookCommand(
                        "evt-2",
                        "sig-valid",
                        "{\"type\":\"payment.authorized\"}"
                );

        when(paymentWebhookSignaturePort.isValidSignature("sig-valid", "{\"type\":\"payment.authorized\"}"))
                .thenReturn(true);
        when(paymentWebhookReceiptPort.findByProviderEventId("evt-2")).thenReturn(Optional.empty());
        when(paymentWebhookReceiptPort.save(any()))
                .thenAnswer(invocation -> {
                    PaymentWebhookReceiptPort.PaymentWebhookReceipt receipt = invocation.getArgument(0);
                    return new PaymentWebhookReceiptPort.PaymentWebhookReceipt(
                            200L,
                            receipt.providerEventId(),
                            receipt.signature(),
                            receipt.payload(),
                            receipt.signatureValid(),
                            receipt.reconciliationStatus(),
                            receipt.receivedAt(),
                            receipt.updatedAt()
                    );
                });

        HandlePaymentWebhookUseCase.HandlePaymentWebhookResult result = paymentService.handleWebhook(command);

        assertEquals("ACCEPTED", result.ingestStatus());
        assertEquals("NO_OP", result.reconciliationStatus());
        assertEquals("evt-2", result.providerEventId());
        assertEquals(200L, result.receiptId());
        verify(paymentWebhookReceiptPort).save(any());
    }

    @Test
    void shouldNotPersistDuplicateWebhookEventAndReturnNoOp() {
        HandlePaymentWebhookUseCase.HandlePaymentWebhookCommand command =
                new HandlePaymentWebhookUseCase.HandlePaymentWebhookCommand(
                        "evt-3",
                        "sig-valid",
                        "{\"type\":\"payment.captured\"}"
                );

        when(paymentWebhookSignaturePort.isValidSignature("sig-valid", "{\"type\":\"payment.captured\"}"))
                .thenReturn(true);
        when(paymentWebhookReceiptPort.findByProviderEventId("evt-3")).thenReturn(Optional.of(
                new PaymentWebhookReceiptPort.PaymentWebhookReceipt(
                        300L,
                        "evt-3",
                        "sig-valid",
                        "{\"type\":\"payment.captured\"}",
                        true,
                        "NO_OP",
                        LocalDateTime.now(),
                        LocalDateTime.now()
                )
        ));

        HandlePaymentWebhookUseCase.HandlePaymentWebhookResult result = paymentService.handleWebhook(command);

        assertEquals("DUPLICATE", result.ingestStatus());
        assertEquals("NO_OP", result.reconciliationStatus());
        assertEquals(300L, result.receiptId());
        verify(paymentWebhookReceiptPort, never()).save(any());
    }
}

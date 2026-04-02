package com.example.ecommerce.payment.domain.model;

import com.example.ecommerce.payment.domain.exception.InvalidPaymentTransitionException;
import com.example.ecommerce.payment.domain.exception.UnsupportedPaymentOperationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentTest {

    @Test
    void shouldTransitionFromInitiatedToAuthorizedToCaptured() {
        Payment payment = Payment.initiate(10L, 20L, new Money(new BigDecimal("49.99"), new CurrencyCode("USD")));

        payment.authorize(new ProviderReference("AUTH-1"));
        payment.capture(new ProviderReference("CAP-1"));

        assertEquals(PaymentStatus.CAPTURED, payment.getStatus());
        assertEquals("CAP-1", payment.getProviderReference().getValue());
    }

    @Test
    void shouldRejectInvalidTransitionFromFailedToCapture() {
        Payment payment = Payment.initiate(10L, 20L, new Money(new BigDecimal("49.99"), new CurrencyCode("USD")));
        payment.fail("provider_error");

        assertThrows(InvalidPaymentTransitionException.class, () ->
                payment.capture(new ProviderReference("CAP-1"))
        );
    }

    @Test
    void shouldRejectRefundInMvp() {
        Payment payment = Payment.initiate(10L, 20L, new Money(new BigDecimal("49.99"), new CurrencyCode("USD")));

        assertThrows(UnsupportedPaymentOperationException.class, payment::refund);
    }
}

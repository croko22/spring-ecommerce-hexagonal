package com.example.ecommerce.payment.infrastructure.adapter.out.provider;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StubPaymentWebhookSignatureAdapterTest {

    private final StubPaymentWebhookSignatureAdapter adapter = new StubPaymentWebhookSignatureAdapter();

    @Test
    void shouldAcceptSignatureWhenItMatchesStubContract() {
        boolean valid = adapter.isValidSignature("sig-valid", "{\"ok\":true}");

        assertTrue(valid);
    }

    @Test
    void shouldRejectSignatureWhenPrefixIsInvalid() {
        boolean valid = adapter.isValidSignature("invalid", "{\"ok\":true}");

        assertFalse(valid);
    }
}

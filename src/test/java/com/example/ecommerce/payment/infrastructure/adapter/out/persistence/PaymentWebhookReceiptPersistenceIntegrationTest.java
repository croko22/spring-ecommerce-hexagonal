package com.example.ecommerce.payment.infrastructure.adapter.out.persistence;

import com.example.ecommerce.payment.application.port.out.PaymentWebhookReceiptPort;
import com.example.ecommerce.shared.infrastructure.PostgresContainerIntegrationTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = "feature.payment.enabled=true")
@Transactional
@Tag("integration")
class PaymentWebhookReceiptPersistenceIntegrationTest extends PostgresContainerIntegrationTest {

    @Autowired
    private PaymentWebhookReceiptJpaRepository repository;

    @Autowired
    private PaymentWebhookReceiptPersistenceAdapter adapter;

    @Test
    void shouldPersistAndFindWebhookReceiptByProviderEventId() {
        PaymentWebhookReceiptPort.PaymentWebhookReceipt saved = adapter.save(
                new PaymentWebhookReceiptPort.PaymentWebhookReceipt(
                        null,
                        "evt-100",
                        "sig-valid",
                        "{\"type\":\"payment.captured\"}",
                        true,
                        "NO_OP",
                        null,
                        null
                )
        );

        assertTrue(saved.id() != null && saved.id() > 0);

        PaymentWebhookReceiptPort.PaymentWebhookReceipt found = adapter.findByProviderEventId("evt-100").orElseThrow();
        assertEquals(saved.id(), found.id());
        assertEquals("NO_OP", found.reconciliationStatus());
    }

    @Test
    void shouldEnforceProviderEventIdUniqueness() {
        PaymentWebhookReceiptEntity first = new PaymentWebhookReceiptEntity();
        first.setProviderEventId("evt-unique");
        first.setSignature("sig-a");
        first.setPayload("{}");
        first.setSignatureValid(true);
        first.setReconciliationStatus("NO_OP");
        first.setReceivedAt(LocalDateTime.now());
        first.setUpdatedAt(LocalDateTime.now());
        repository.saveAndFlush(first);

        PaymentWebhookReceiptEntity duplicate = new PaymentWebhookReceiptEntity();
        duplicate.setProviderEventId("evt-unique");
        duplicate.setSignature("sig-b");
        duplicate.setPayload("{}");
        duplicate.setSignatureValid(true);
        duplicate.setReconciliationStatus("NO_OP");
        duplicate.setReceivedAt(LocalDateTime.now());
        duplicate.setUpdatedAt(LocalDateTime.now());

        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(duplicate));
    }
}

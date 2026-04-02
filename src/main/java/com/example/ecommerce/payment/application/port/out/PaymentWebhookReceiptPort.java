package com.example.ecommerce.payment.application.port.out;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PaymentWebhookReceiptPort {

    Optional<PaymentWebhookReceipt> findByProviderEventId(String providerEventId);

    PaymentWebhookReceipt save(PaymentWebhookReceipt receipt);

    record PaymentWebhookReceipt(
            Long id,
            String providerEventId,
            String signature,
            String payload,
            boolean signatureValid,
            String reconciliationStatus,
            LocalDateTime receivedAt,
            LocalDateTime updatedAt
    ) {
    }
}

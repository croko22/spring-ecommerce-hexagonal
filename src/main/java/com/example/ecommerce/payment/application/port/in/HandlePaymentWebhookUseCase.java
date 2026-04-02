package com.example.ecommerce.payment.application.port.in;

public interface HandlePaymentWebhookUseCase {

    HandlePaymentWebhookResult handleWebhook(HandlePaymentWebhookCommand command);

    record HandlePaymentWebhookCommand(
            String providerEventId,
            String signature,
            String payload
    ) {
    }

    record HandlePaymentWebhookResult(
            Long receiptId,
            String providerEventId,
            String ingestStatus,
            String reconciliationStatus
    ) {
    }
}

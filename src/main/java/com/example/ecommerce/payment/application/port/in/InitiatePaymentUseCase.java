package com.example.ecommerce.payment.application.port.in;

import java.math.BigDecimal;

public interface InitiatePaymentUseCase {

    InitiatePaymentResult initiatePayment(InitiatePaymentCommand command);

    record InitiatePaymentCommand(
            Long orderId,
            String idempotencyKey,
            String paymentMethodToken
    ) {
    }

    record InitiatePaymentResult(
            Long paymentId,
            String status,
            BigDecimal amount,
            String currency,
            Long orderId,
            String providerReference
    ) {
    }
}

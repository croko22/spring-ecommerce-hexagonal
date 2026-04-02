package com.example.ecommerce.payment.application.port.in;

import java.math.BigDecimal;

public interface GetPaymentUseCase {

    PaymentDetails getPaymentById(Long paymentId);

    record PaymentDetails(
            Long paymentId,
            Long orderId,
            Long userId,
            String status,
            BigDecimal amount,
            String currency,
            String providerReference,
            String failureCode
    ) {
    }
}

package com.example.ecommerce.payment.application.port.out;

public interface PaymentProviderPort {

    ProviderAuthorizeCaptureResult authorizeAndCapture(
            Long orderId,
            Long userId,
            String paymentMethodToken
    );

    record ProviderAuthorizeCaptureResult(
            boolean success,
            String providerReference,
            String failureCode
    ) {
    }
}

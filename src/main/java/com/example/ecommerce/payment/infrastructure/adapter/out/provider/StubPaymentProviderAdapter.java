package com.example.ecommerce.payment.infrastructure.adapter.out.provider;

import com.example.ecommerce.payment.application.port.out.PaymentProviderPort;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class StubPaymentProviderAdapter implements PaymentProviderPort {

    @Override
    public ProviderAuthorizeCaptureResult authorizeAndCapture(Long orderId, Long userId, String paymentMethodToken) {
        if (paymentMethodToken != null && paymentMethodToken.toLowerCase().contains("fail")) {
            return new ProviderAuthorizeCaptureResult(false, null, "PROVIDER_DECLINED");
        }

        String providerReference = "stub-" + UUID.randomUUID();
        return new ProviderAuthorizeCaptureResult(true, providerReference, null);
    }
}

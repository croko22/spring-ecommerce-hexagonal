package com.example.ecommerce.payment.infrastructure.adapter.out.provider;

import com.example.ecommerce.payment.application.port.out.PaymentWebhookSignaturePort;
import org.springframework.stereotype.Component;

@Component
public class StubPaymentWebhookSignatureAdapter implements PaymentWebhookSignaturePort {

    @Override
    public boolean isValidSignature(String signature, String payload) {
        return signature != null
                && !signature.isBlank()
                && payload != null
                && !payload.isBlank()
                && signature.startsWith("sig-");
    }
}

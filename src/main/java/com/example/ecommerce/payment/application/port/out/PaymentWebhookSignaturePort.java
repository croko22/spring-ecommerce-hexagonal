package com.example.ecommerce.payment.application.port.out;

public interface PaymentWebhookSignaturePort {

    boolean isValidSignature(String signature, String payload);
}

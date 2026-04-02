package com.example.ecommerce.payment.application.exception;

public class PaymentWebhookSignatureInvalidException extends RuntimeException {

    public PaymentWebhookSignatureInvalidException(String message) {
        super(message);
    }
}

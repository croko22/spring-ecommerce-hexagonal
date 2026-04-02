package com.example.ecommerce.payment.application.exception;

public class PaymentFeatureDisabledException extends RuntimeException {

    public PaymentFeatureDisabledException(String message) {
        super(message);
    }
}

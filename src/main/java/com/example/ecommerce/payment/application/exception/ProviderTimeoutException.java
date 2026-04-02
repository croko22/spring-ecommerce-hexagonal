package com.example.ecommerce.payment.application.exception;

public class ProviderTimeoutException extends RuntimeException {

    public ProviderTimeoutException(String message) {
        super(message);
    }
}

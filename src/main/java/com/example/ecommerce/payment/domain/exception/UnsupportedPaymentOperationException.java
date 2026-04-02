package com.example.ecommerce.payment.domain.exception;

public class UnsupportedPaymentOperationException extends RuntimeException {

    public UnsupportedPaymentOperationException(String message) {
        super(message);
    }
}

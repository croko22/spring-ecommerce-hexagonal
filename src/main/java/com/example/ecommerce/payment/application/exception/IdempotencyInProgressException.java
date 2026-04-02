package com.example.ecommerce.payment.application.exception;

public class IdempotencyInProgressException extends RuntimeException {

    public IdempotencyInProgressException(String message) {
        super(message);
    }
}

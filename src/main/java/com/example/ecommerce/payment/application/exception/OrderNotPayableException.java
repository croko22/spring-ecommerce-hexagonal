package com.example.ecommerce.payment.application.exception;

public class OrderNotPayableException extends RuntimeException {

    public OrderNotPayableException(Long orderId, String status) {
        super("Order " + orderId + " is not payable from status " + status);
    }
}

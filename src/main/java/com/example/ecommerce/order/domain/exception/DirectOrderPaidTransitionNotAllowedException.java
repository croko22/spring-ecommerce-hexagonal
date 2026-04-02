package com.example.ecommerce.order.domain.exception;

public class DirectOrderPaidTransitionNotAllowedException extends RuntimeException {

    public DirectOrderPaidTransitionNotAllowedException() {
        super("Direct order status update to PAID is not allowed. Use POST /api/payments to complete payment.");
    }
}

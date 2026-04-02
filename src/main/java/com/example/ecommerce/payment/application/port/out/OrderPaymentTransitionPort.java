package com.example.ecommerce.payment.application.port.out;

public interface OrderPaymentTransitionPort {

    void markPaidAfterPayment(Long orderId, Long paymentId);
}

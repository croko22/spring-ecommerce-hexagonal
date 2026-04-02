package com.example.ecommerce.payment.application.port.out;

import com.example.ecommerce.payment.domain.model.Payment;

import java.util.Optional;

public interface PaymentRepositoryPort {

    Payment save(Payment payment);

    Optional<Payment> findById(Long id);
}

package com.example.ecommerce.payment.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentWebhookReceiptJpaRepository extends JpaRepository<PaymentWebhookReceiptEntity, Long> {

    Optional<PaymentWebhookReceiptEntity> findByProviderEventId(String providerEventId);
}

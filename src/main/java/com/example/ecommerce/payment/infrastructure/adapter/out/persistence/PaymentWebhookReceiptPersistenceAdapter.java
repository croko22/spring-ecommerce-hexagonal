package com.example.ecommerce.payment.infrastructure.adapter.out.persistence;

import com.example.ecommerce.payment.application.port.out.PaymentWebhookReceiptPort;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class PaymentWebhookReceiptPersistenceAdapter implements PaymentWebhookReceiptPort {

    private final PaymentWebhookReceiptJpaRepository repository;

    public PaymentWebhookReceiptPersistenceAdapter(PaymentWebhookReceiptJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<PaymentWebhookReceipt> findByProviderEventId(String providerEventId) {
        return repository.findByProviderEventId(providerEventId).map(this::toPortRecord);
    }

    @Override
    public PaymentWebhookReceipt save(PaymentWebhookReceipt receipt) {
        LocalDateTime now = LocalDateTime.now();

        PaymentWebhookReceiptEntity entity = new PaymentWebhookReceiptEntity();
        entity.setId(receipt.id());
        entity.setProviderEventId(receipt.providerEventId());
        entity.setSignature(receipt.signature());
        entity.setPayload(receipt.payload());
        entity.setSignatureValid(receipt.signatureValid());
        entity.setReconciliationStatus(receipt.reconciliationStatus());
        entity.setReceivedAt(receipt.receivedAt() != null ? receipt.receivedAt() : now);
        entity.setUpdatedAt(now);

        return toPortRecord(repository.save(entity));
    }

    private PaymentWebhookReceipt toPortRecord(PaymentWebhookReceiptEntity entity) {
        return new PaymentWebhookReceipt(
                entity.getId(),
                entity.getProviderEventId(),
                entity.getSignature(),
                entity.getPayload(),
                entity.isSignatureValid(),
                entity.getReconciliationStatus(),
                entity.getReceivedAt(),
                entity.getUpdatedAt()
        );
    }
}

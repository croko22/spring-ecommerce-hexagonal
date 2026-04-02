package com.example.ecommerce.payment.infrastructure.adapter.out.persistence;

import com.example.ecommerce.payment.application.port.out.PaymentRepositoryPort;
import com.example.ecommerce.payment.application.exception.PaymentFeatureDisabledException;
import com.example.ecommerce.payment.domain.model.Payment;
import com.example.ecommerce.payment.infrastructure.config.PaymentFeatureProperties;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class PaymentPersistenceAdapter implements PaymentRepositoryPort {

    private final PaymentJpaRepository paymentJpaRepository;
    private final PaymentFeatureProperties paymentFeatureProperties;

    public PaymentPersistenceAdapter(
            PaymentJpaRepository paymentJpaRepository,
            PaymentFeatureProperties paymentFeatureProperties
    ) {
        this.paymentJpaRepository = paymentJpaRepository;
        this.paymentFeatureProperties = paymentFeatureProperties;
    }

    @Override
    public Payment save(Payment payment) {
        assertWriteEnabled();
        PaymentEntity entity = PaymentEntity.fromDomain(payment);
        PaymentEntity savedEntity = paymentJpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return paymentJpaRepository.findById(id).map(PaymentEntity::toDomain);
    }

    private void assertWriteEnabled() {
        if (!paymentFeatureProperties.isEnabled()) {
            throw new PaymentFeatureDisabledException("Payment feature is disabled");
        }
    }
}

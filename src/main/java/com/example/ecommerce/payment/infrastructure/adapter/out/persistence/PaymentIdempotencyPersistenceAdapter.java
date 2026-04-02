package com.example.ecommerce.payment.infrastructure.adapter.out.persistence;

import com.example.ecommerce.payment.application.exception.IdempotencyConflictException;
import com.example.ecommerce.payment.application.exception.PaymentFeatureDisabledException;
import com.example.ecommerce.payment.application.port.out.PaymentIdempotencyPort;
import com.example.ecommerce.payment.domain.model.IdempotencyKey;
import com.example.ecommerce.payment.domain.model.PaymentOperation;
import com.example.ecommerce.payment.infrastructure.config.PaymentFeatureProperties;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class PaymentIdempotencyPersistenceAdapter implements PaymentIdempotencyPort {

    private final PaymentIdempotencyJpaRepository repository;
    private final PaymentFeatureProperties paymentFeatureProperties;

    public PaymentIdempotencyPersistenceAdapter(
            PaymentIdempotencyJpaRepository repository,
            PaymentFeatureProperties paymentFeatureProperties
    ) {
        this.repository = repository;
        this.paymentFeatureProperties = paymentFeatureProperties;
    }

    @Override
    @Transactional
    public void reserveOrValidate(
            PaymentOperation operation,
            String actorScope,
            IdempotencyKey idempotencyKey,
            String requestHash
    ) {
        assertWriteEnabled();
        Optional<PaymentIdempotencyEntity> existing = repository.findByOperationAndActorScopeAndIdempotencyKey(
                operation,
                actorScope,
                idempotencyKey.getValue()
        );

        if (existing.isPresent()) {
            validateRequestHash(existing.get(), requestHash, idempotencyKey);
            return;
        }

        PaymentIdempotencyEntity entity = new PaymentIdempotencyEntity();
        entity.setOperation(operation);
        entity.setActorScope(actorScope);
        entity.setIdempotencyKey(idempotencyKey.getValue());
        entity.setRequestHash(requestHash);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        try {
            repository.save(entity);
        } catch (DataIntegrityViolationException ex) {
            PaymentIdempotencyEntity concurrent = repository.findByOperationAndActorScopeAndIdempotencyKey(
                    operation,
                    actorScope,
                    idempotencyKey.getValue()
            ).orElseThrow(() -> ex);
            validateRequestHash(concurrent, requestHash, idempotencyKey);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StoredResponse> findStoredResponse(
            PaymentOperation operation,
            String actorScope,
            IdempotencyKey idempotencyKey
    ) {
        return repository.findByOperationAndActorScopeAndIdempotencyKey(operation, actorScope, idempotencyKey.getValue())
                .filter(entity -> entity.getResponseStatus() != null)
                .map(entity -> new StoredResponse(
                        entity.getPaymentId(),
                        entity.getResponseStatus(),
                        entity.getResponseBody(),
                        entity.getRequestHash()
                ));
    }

    @Override
    @Transactional
    public void saveResult(
            PaymentOperation operation,
            String actorScope,
            IdempotencyKey idempotencyKey,
            String requestHash,
            Long paymentId,
            String responseStatus,
            String responseBody
    ) {
        assertWriteEnabled();
        PaymentIdempotencyEntity entity = repository.findByOperationAndActorScopeAndIdempotencyKey(
                operation,
                actorScope,
                idempotencyKey.getValue()
        ).orElseThrow(() -> new IllegalStateException("Idempotency reservation not found"));

        validateRequestHash(entity, requestHash, idempotencyKey);

        entity.setPaymentId(paymentId);
        entity.setResponseStatus(responseStatus);
        entity.setResponseBody(responseBody);
        entity.setUpdatedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private void validateRequestHash(
            PaymentIdempotencyEntity entity,
            String requestHash,
            IdempotencyKey idempotencyKey
    ) {
        if (!entity.getRequestHash().equals(requestHash)) {
            throw new IdempotencyConflictException(
                    "Idempotency key conflict for key " + idempotencyKey.getValue()
            );
        }
    }

    private void assertWriteEnabled() {
        if (!paymentFeatureProperties.isEnabled()) {
            throw new PaymentFeatureDisabledException("Payment feature is disabled");
        }
    }
}

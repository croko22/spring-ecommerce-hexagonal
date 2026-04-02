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
    public AcquireOutcome acquireOrReplay(
            PaymentOperation operation,
            String actorScope,
            IdempotencyKey idempotencyKey,
            String requestHash
    ) {
        assertWriteEnabled();
        PaymentIdempotencyEntity existing = repository.findByOperationAndActorScopeAndIdempotencyKey(
                operation,
                actorScope,
                idempotencyKey.getValue()
        ).orElse(null);

        if (existing != null) {
            validateRequestHash(existing, requestHash, idempotencyKey);
            return toAcquireOutcome(existing);
        }

        PaymentIdempotencyEntity entity = new PaymentIdempotencyEntity();
        entity.setOperation(operation);
        entity.setActorScope(actorScope);
        entity.setIdempotencyKey(idempotencyKey.getValue());
        entity.setRequestHash(requestHash);
        entity.setStatus(PaymentIdempotencyEntity.ProcessingStatus.IN_PROGRESS);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        try {
            repository.save(entity);
            return new Acquired();
        } catch (DataIntegrityViolationException ex) {
            PaymentIdempotencyEntity concurrent = repository.findByOperationAndActorScopeAndIdempotencyKey(
                    operation,
                    actorScope,
                    idempotencyKey.getValue()
            ).orElseThrow(() -> ex);
            validateRequestHash(concurrent, requestHash, idempotencyKey);
            return toAcquireOutcome(concurrent);
        }
    }

    @Override
    @Transactional
    public void complete(
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
        entity.setStatus(PaymentIdempotencyEntity.ProcessingStatus.COMPLETED);
        entity.setUpdatedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private AcquireOutcome toAcquireOutcome(PaymentIdempotencyEntity entity) {
        PaymentIdempotencyEntity.ProcessingStatus status = resolveStatus(entity);
        if (status == PaymentIdempotencyEntity.ProcessingStatus.COMPLETED) {
            return new Replay(new StoredResponse(
                    entity.getPaymentId(),
                    entity.getResponseStatus(),
                    entity.getResponseBody(),
                    entity.getRequestHash()
            ));
        }
        return new InProgress();
    }

    private PaymentIdempotencyEntity.ProcessingStatus resolveStatus(PaymentIdempotencyEntity entity) {
        if (entity.getStatus() != null) {
            return entity.getStatus();
        }
        if (entity.getResponseStatus() != null) {
            return PaymentIdempotencyEntity.ProcessingStatus.COMPLETED;
        }
        return PaymentIdempotencyEntity.ProcessingStatus.IN_PROGRESS;
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

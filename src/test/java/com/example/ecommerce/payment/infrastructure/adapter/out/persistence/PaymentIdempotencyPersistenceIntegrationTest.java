package com.example.ecommerce.payment.infrastructure.adapter.out.persistence;

import com.example.ecommerce.payment.application.exception.IdempotencyConflictException;
import com.example.ecommerce.payment.domain.model.IdempotencyKey;
import com.example.ecommerce.payment.domain.model.PaymentOperation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(properties = "feature.payment.enabled=true")
@Transactional
class PaymentIdempotencyPersistenceIntegrationTest {

    @Autowired
    private PaymentIdempotencyJpaRepository repository;

    @Autowired
    private PaymentIdempotencyPersistenceAdapter adapter;

    @Test
    void shouldEnforceUniqueTupleOperationActorScopeAndIdempotencyKey() {
        PaymentIdempotencyEntity first = baseEntity();
        repository.saveAndFlush(first);

        PaymentIdempotencyEntity duplicate = baseEntity();
        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(duplicate));
    }

    @Test
    void shouldRejectSameTupleWithDifferentRequestHash() {
        adapter.reserveOrValidate(PaymentOperation.INITIATE, "u1:o1", new IdempotencyKey("key-1"), "hash-a");

        assertThrows(IdempotencyConflictException.class, () ->
                adapter.reserveOrValidate(PaymentOperation.INITIATE, "u1:o1", new IdempotencyKey("key-1"), "hash-b")
        );
    }

    private PaymentIdempotencyEntity baseEntity() {
        PaymentIdempotencyEntity entity = new PaymentIdempotencyEntity();
        entity.setOperation(PaymentOperation.INITIATE);
        entity.setActorScope("u1:o1");
        entity.setIdempotencyKey("key-1");
        entity.setRequestHash("hash-a");
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }
}

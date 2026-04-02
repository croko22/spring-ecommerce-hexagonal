package com.example.ecommerce.payment.infrastructure.adapter.out.persistence;

import com.example.ecommerce.payment.application.exception.IdempotencyConflictException;
import com.example.ecommerce.payment.application.port.out.PaymentIdempotencyPort;
import com.example.ecommerce.payment.domain.model.IdempotencyKey;
import com.example.ecommerce.payment.domain.model.PaymentOperation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        PaymentIdempotencyPort.AcquireOutcome first = adapter.acquireOrReplay(
                PaymentOperation.INITIATE,
                "u1:o1",
                new IdempotencyKey("key-1"),
                "hash-a"
        );
        assertTrue(first instanceof PaymentIdempotencyPort.Acquired);

        assertThrows(IdempotencyConflictException.class, () ->
                adapter.acquireOrReplay(PaymentOperation.INITIATE, "u1:o1", new IdempotencyKey("key-1"), "hash-b")
        );
    }

    @Test
    void shouldTransitionFromInProgressToCompletedAndReplayStoredResult() {
        PaymentIdempotencyPort.AcquireOutcome initial = adapter.acquireOrReplay(
                PaymentOperation.INITIATE,
                "u2:o2",
                new IdempotencyKey("key-2"),
                "hash-z"
        );
        assertTrue(initial instanceof PaymentIdempotencyPort.Acquired);

        PaymentIdempotencyPort.AcquireOutcome duplicateWhileRunning = adapter.acquireOrReplay(
                PaymentOperation.INITIATE,
                "u2:o2",
                new IdempotencyKey("key-2"),
                "hash-z"
        );
        assertTrue(duplicateWhileRunning instanceof PaymentIdempotencyPort.InProgress);

        adapter.complete(
                PaymentOperation.INITIATE,
                "u2:o2",
                new IdempotencyKey("key-2"),
                "hash-z",
                222L,
                "CAPTURED",
                "222|CAPTURED|14.99|USD|2|prov-222"
        );

        Optional<PaymentIdempotencyEntity> stored = repository.findByOperationAndActorScopeAndIdempotencyKey(
                PaymentOperation.INITIATE,
                "u2:o2",
                "key-2"
        );
        assertTrue(stored.isPresent());
        assertEquals(PaymentIdempotencyEntity.ProcessingStatus.COMPLETED, stored.get().getStatus());

        PaymentIdempotencyPort.AcquireOutcome replay = adapter.acquireOrReplay(
                PaymentOperation.INITIATE,
                "u2:o2",
                new IdempotencyKey("key-2"),
                "hash-z"
        );

        assertTrue(replay instanceof PaymentIdempotencyPort.Replay);
        PaymentIdempotencyPort.Replay replayResult = (PaymentIdempotencyPort.Replay) replay;
        assertEquals(222L, replayResult.storedResponse().paymentId());
        assertEquals("CAPTURED", replayResult.storedResponse().responseStatus());
        assertEquals("222|CAPTURED|14.99|USD|2|prov-222", replayResult.storedResponse().responseBody());
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

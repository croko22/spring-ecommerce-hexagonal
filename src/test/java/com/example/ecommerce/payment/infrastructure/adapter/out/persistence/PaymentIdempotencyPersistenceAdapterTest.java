package com.example.ecommerce.payment.infrastructure.adapter.out.persistence;

import com.example.ecommerce.payment.application.exception.IdempotencyConflictException;
import com.example.ecommerce.payment.application.exception.PaymentFeatureDisabledException;
import com.example.ecommerce.payment.application.port.out.PaymentIdempotencyPort;
import com.example.ecommerce.payment.domain.model.IdempotencyKey;
import com.example.ecommerce.payment.domain.model.PaymentOperation;
import com.example.ecommerce.payment.infrastructure.config.PaymentFeatureProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentIdempotencyPersistenceAdapterTest {

    @Mock
    private PaymentIdempotencyJpaRepository repository;

    private PaymentFeatureProperties featureProperties;
    private PaymentIdempotencyPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        featureProperties = new PaymentFeatureProperties();
        featureProperties.setEnabled(true);
        adapter = new PaymentIdempotencyPersistenceAdapter(repository, featureProperties);
    }

    @Test
    void shouldThrowConflictWhenSameTupleHasDifferentRequestHash() {
        PaymentIdempotencyEntity existing = new PaymentIdempotencyEntity();
        existing.setOperation(PaymentOperation.INITIATE);
        existing.setActorScope("u1:o1");
        existing.setIdempotencyKey("key-1");
        existing.setRequestHash("hash-a");

        when(repository.findByOperationAndActorScopeAndIdempotencyKey(PaymentOperation.INITIATE, "u1:o1", "key-1"))
                .thenReturn(Optional.of(existing));

        assertThrows(IdempotencyConflictException.class, () ->
                adapter.reserveOrValidate(PaymentOperation.INITIATE, "u1:o1", new IdempotencyKey("key-1"), "hash-b")
        );
    }

    @Test
    void shouldReturnStoredResponseForDuplicateSamePayload() {
        PaymentIdempotencyEntity existing = new PaymentIdempotencyEntity();
        existing.setOperation(PaymentOperation.INITIATE);
        existing.setActorScope("u1:o1");
        existing.setIdempotencyKey("key-1");
        existing.setRequestHash("hash-a");
        existing.setPaymentId(99L);
        existing.setResponseStatus("CAPTURED");
        existing.setResponseBody("{\"paymentId\":99}");

        when(repository.findByOperationAndActorScopeAndIdempotencyKey(PaymentOperation.INITIATE, "u1:o1", "key-1"))
                .thenReturn(Optional.of(existing));

        Optional<PaymentIdempotencyPort.StoredResponse> result = adapter.findStoredResponse(
                PaymentOperation.INITIATE,
                "u1:o1",
                new IdempotencyKey("key-1")
        );

        assertTrue(result.isPresent());
        assertEquals(99L, result.get().paymentId());
        assertEquals("CAPTURED", result.get().responseStatus());
    }

    @Test
    void shouldThrowWhenPaymentFeatureIsDisabledForWriteOperations() {
        featureProperties.setEnabled(false);

        assertThrows(PaymentFeatureDisabledException.class, () ->
                adapter.reserveOrValidate(PaymentOperation.INITIATE, "u1:o1", new IdempotencyKey("key-1"), "hash-a")
        );
    }
}

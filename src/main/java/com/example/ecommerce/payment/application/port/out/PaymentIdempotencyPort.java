package com.example.ecommerce.payment.application.port.out;

import com.example.ecommerce.payment.domain.model.IdempotencyKey;
import com.example.ecommerce.payment.domain.model.PaymentOperation;

public interface PaymentIdempotencyPort {

    AcquireOutcome acquireOrReplay(
            PaymentOperation operation,
            String actorScope,
            IdempotencyKey idempotencyKey,
            String requestHash
    );

    void complete(
            PaymentOperation operation,
            String actorScope,
            IdempotencyKey idempotencyKey,
            String requestHash,
            Long paymentId,
            String responseStatus,
            String responseBody
    );

    sealed interface AcquireOutcome permits Acquired, Replay, InProgress {
    }

    record Acquired() implements AcquireOutcome {
    }

    record Replay(StoredResponse storedResponse) implements AcquireOutcome {
    }

    record InProgress() implements AcquireOutcome {
    }

    record StoredResponse(
            Long paymentId,
            String responseStatus,
            String responseBody,
            String requestHash
    ) {
    }
}

package com.example.ecommerce.payment.application.port.out;

import com.example.ecommerce.payment.domain.model.IdempotencyKey;
import com.example.ecommerce.payment.domain.model.PaymentOperation;

import java.util.Optional;

public interface PaymentIdempotencyPort {

    void reserveOrValidate(
            PaymentOperation operation,
            String actorScope,
            IdempotencyKey idempotencyKey,
            String requestHash
    );

    Optional<StoredResponse> findStoredResponse(
            PaymentOperation operation,
            String actorScope,
            IdempotencyKey idempotencyKey
    );

    void saveResult(
            PaymentOperation operation,
            String actorScope,
            IdempotencyKey idempotencyKey,
            String requestHash,
            Long paymentId,
            String responseStatus,
            String responseBody
    );

    record StoredResponse(
            Long paymentId,
            String responseStatus,
            String responseBody,
            String requestHash
    ) {
    }
}

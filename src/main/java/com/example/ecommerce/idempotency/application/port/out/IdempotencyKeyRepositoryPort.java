package com.example.ecommerce.idempotency.application.port.out;

import com.example.ecommerce.idempotency.domain.model.IdempotencyKey;

import java.util.Optional;

public interface IdempotencyKeyRepositoryPort {

    Optional<StoredResponse> findByIdempotencyKeyAndResource(IdempotencyKey idempotencyKey, String resourcePath);

    boolean tryReserve(IdempotencyKey idempotencyKey, String resourcePath, String requestHash);

    void complete(IdempotencyKey idempotencyKey, String resourcePath, String requestHash,
                  int responseStatus, String responseBody);

    record StoredResponse(int responseStatus, String responseBody) {}
}
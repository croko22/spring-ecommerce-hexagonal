package com.example.ecommerce.idempotency.infrastructure.adapter.out.persistence;

import com.example.ecommerce.idempotency.application.port.out.IdempotencyKeyRepositoryPort;
import com.example.ecommerce.idempotency.domain.model.IdempotencyKey;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class IdempotencyKeyRepositoryAdapter implements IdempotencyKeyRepositoryPort {

    private final IdempotencyKeyJpaRepository jpaRepository;

    public IdempotencyKeyRepositoryAdapter(IdempotencyKeyJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StoredResponse> findByIdempotencyKeyAndResource(IdempotencyKey idempotencyKey, String resourcePath) {
        return jpaRepository.findByIdempotencyKeyAndResourcePath(idempotencyKey.getValue(), resourcePath)
                .filter(entity -> entity.getResponseStatus() != null)
                .map(entity -> new StoredResponse(entity.getResponseStatus(), entity.getResponseBody()));
    }

    @Override
    @Transactional
    public boolean tryReserve(IdempotencyKey idempotencyKey, String resourcePath, String requestHash) {
        try {
            IdempotencyKeyEntity entity = new IdempotencyKeyEntity();
            entity.setIdempotencyKey(idempotencyKey.getValue());
            entity.setResourcePath(resourcePath);
            entity.setRequestHash(requestHash);
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());
            jpaRepository.save(entity);
            jpaRepository.flush();
            return true;
        } catch (DataIntegrityViolationException e) {
            return false;
        }
    }

    @Override
    @Transactional
    public void complete(IdempotencyKey idempotencyKey, String resourcePath, String requestHash,
                         int responseStatus, String responseBody) {
        IdempotencyKeyEntity entity = jpaRepository
                .findByIdempotencyKeyAndResourcePath(idempotencyKey.getValue(), resourcePath)
                .orElseThrow(() -> new IllegalStateException("Idempotency record not found"));
        entity.setResponseStatus(responseStatus);
        entity.setResponseBody(responseBody);
        entity.setRequestHash(requestHash);
        entity.setUpdatedAt(LocalDateTime.now());
        jpaRepository.save(entity);
    }
}
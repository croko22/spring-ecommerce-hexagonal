package com.example.ecommerce.idempotency.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IdempotencyKeyJpaRepository extends JpaRepository<IdempotencyKeyEntity, Long> {

    Optional<IdempotencyKeyEntity> findByIdempotencyKeyAndResourcePath(String idempotencyKey, String resourcePath);

    boolean existsByIdempotencyKeyAndResourcePath(String idempotencyKey, String resourcePath);
}
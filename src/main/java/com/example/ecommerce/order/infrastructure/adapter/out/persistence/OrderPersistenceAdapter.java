package com.example.ecommerce.order.infrastructure.adapter.out.persistence;

import com.example.ecommerce.order.application.port.out.OrderRepositoryPort;
import com.example.ecommerce.order.domain.model.Order;
import com.example.ecommerce.order.domain.model.OrderStatus;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class OrderPersistenceAdapter implements OrderRepositoryPort {

    private final OrderJpaRepository orderJpaRepository;
    private final EntityManager entityManager;

    public OrderPersistenceAdapter(OrderJpaRepository orderJpaRepository, EntityManager entityManager) {
        this.orderJpaRepository = orderJpaRepository;
        this.entityManager = entityManager;
    }

    @Override
    public Order save(Order order) {
        OrderEntity entity = OrderEntity.fromDomain(order);
        OrderEntity savedEntity = orderJpaRepository.save(entity);
        entityManager.flush();
        entityManager.refresh(savedEntity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<Order> findById(Long id) {
        return orderJpaRepository.findById(id).map(OrderEntity::toDomain);
    }

    @Override
    public Optional<Order> findByOrderNumber(String orderNumber) {
        return orderJpaRepository.findByOrderNumber(orderNumber).map(OrderEntity::toDomain);
    }

    @Override
    public List<Order> findByUserId(Long userId) {
        return orderJpaRepository.findAll().stream()
                .filter(entity -> entity.getUserId().equals(userId))
                .map(OrderEntity::toDomain)
                .toList();
    }

    @Override
    public List<Order> findAll() {
        return orderJpaRepository.findAll().stream()
                .map(OrderEntity::toDomain)
                .toList();
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        return orderJpaRepository.findByStatus(status).stream()
                .map(OrderEntity::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        orderJpaRepository.deleteById(id);
    }
}

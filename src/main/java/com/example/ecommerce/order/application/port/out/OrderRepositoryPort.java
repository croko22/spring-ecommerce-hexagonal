package com.example.ecommerce.order.application.port.out;

import com.example.ecommerce.order.domain.model.Order;
import java.util.List;
import java.util.Optional;

public interface OrderRepositoryPort {

    Order save(Order order);

    Optional<Order> findById(Long id);

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByUserId(Long userId);

    void deleteById(Long id);
}

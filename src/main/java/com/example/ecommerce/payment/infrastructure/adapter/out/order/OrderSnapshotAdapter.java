package com.example.ecommerce.payment.infrastructure.adapter.out.order;

import com.example.ecommerce.order.application.port.out.OrderRepositoryPort;
import com.example.ecommerce.order.domain.exception.OrderNotFoundException;
import com.example.ecommerce.order.domain.model.Order;
import com.example.ecommerce.payment.application.port.out.OrderSnapshotPort;
import org.springframework.stereotype.Component;

@Component
public class OrderSnapshotAdapter implements OrderSnapshotPort {

    private static final String DEFAULT_CURRENCY = "USD";

    private final OrderRepositoryPort orderRepositoryPort;

    public OrderSnapshotAdapter(OrderRepositoryPort orderRepositoryPort) {
        this.orderRepositoryPort = orderRepositoryPort;
    }

    @Override
    public OrderSnapshot getOrderSnapshot(Long orderId) {
        Order order = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        return new OrderSnapshot(
                order.getId(),
                order.getUserId(),
                order.getStatus().name(),
                order.getTotalAmount(),
                DEFAULT_CURRENCY
        );
    }
}

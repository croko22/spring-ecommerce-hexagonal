package com.example.ecommerce.payment.infrastructure.adapter.out.order;

import com.example.ecommerce.order.application.port.out.OrderRepositoryPort;
import com.example.ecommerce.order.domain.exception.OrderNotFoundException;
import com.example.ecommerce.order.domain.model.Order;
import com.example.ecommerce.order.domain.model.OrderStatus;
import com.example.ecommerce.payment.application.port.out.OrderPaymentTransitionPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OrderPaymentTransitionAdapter implements OrderPaymentTransitionPort {

    private final OrderRepositoryPort orderRepositoryPort;

    public OrderPaymentTransitionAdapter(OrderRepositoryPort orderRepositoryPort) {
        this.orderRepositoryPort = orderRepositoryPort;
    }

    @Override
    @Transactional
    public void markPaidAfterPayment(Long orderId, Long paymentId) {
        Order order = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() == OrderStatus.PAID) {
            return;
        }

        order.updateStatus(OrderStatus.PAID);
        orderRepositoryPort.save(order);
    }
}

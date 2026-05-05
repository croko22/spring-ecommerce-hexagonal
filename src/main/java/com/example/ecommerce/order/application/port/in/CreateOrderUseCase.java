package com.example.ecommerce.order.application.port.in;

import com.example.ecommerce.order.domain.model.Order;

public interface CreateOrderUseCase {

    Order createOrder(Long userId, CreateOrderCommand command);

    record CreateOrderCommand(
        String street,
        String city,
        String state,
        String zipCode,
        String country,
        String discountCode
    ) {}
}

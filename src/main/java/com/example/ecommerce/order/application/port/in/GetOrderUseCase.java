package com.example.ecommerce.order.application.port.in;

import com.example.ecommerce.order.domain.model.Order;

public interface GetOrderUseCase {

    Order getOrderById(Long orderId);

    Order getOrderByNumber(String orderNumber);
}

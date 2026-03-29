package com.example.ecommerce.order.application.port.in;

import com.example.ecommerce.order.domain.model.Order;
import com.example.ecommerce.order.domain.model.OrderStatus;

public interface UpdateOrderStatusUseCase {

    Order updateOrderStatus(Long orderId, OrderStatus newStatus);
}

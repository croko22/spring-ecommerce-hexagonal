package com.example.ecommerce.order.application.port.in;

import com.example.ecommerce.order.domain.model.Order;
import java.util.List;

public interface GetUserOrdersUseCase {

    List<Order> getUserOrders(Long userId);
}

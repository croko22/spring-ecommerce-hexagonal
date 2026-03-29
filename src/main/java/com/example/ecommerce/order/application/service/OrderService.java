package com.example.ecommerce.order.application.service;

import com.example.ecommerce.cart.domain.model.Cart;
import com.example.ecommerce.cart.domain.model.CartItem;
import com.example.ecommerce.order.application.port.in.*;
import com.example.ecommerce.order.application.port.out.CartPort;
import com.example.ecommerce.order.application.port.out.OrderRepositoryPort;
import com.example.ecommerce.order.domain.exception.OrderNotFoundException;
import com.example.ecommerce.order.domain.model.Order;
import com.example.ecommerce.order.domain.model.OrderItem;
import com.example.ecommerce.order.domain.model.OrderStatus;
import com.example.ecommerce.order.domain.model.ShippingAddress;

import java.util.List;

public class OrderService implements CreateOrderUseCase, GetOrderUseCase, GetUserOrdersUseCase, UpdateOrderStatusUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final CartPort cartPort;

    public OrderService(OrderRepositoryPort orderRepositoryPort, CartPort cartPort) {
        this.orderRepositoryPort = orderRepositoryPort;
        this.cartPort = cartPort;
    }

    @Override
    public Order createOrder(Long userId, CreateOrderCommand command) {
        Cart cart = cartPort.getCartByUserId(userId);
        
        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot create order from empty cart");
        }

        List<OrderItem> orderItems = cart.getItems().stream()
                .map(this::mapToOrderItem)
                .toList();

        ShippingAddress shippingAddress = new ShippingAddress(
                command.street(),
                command.city(),
                command.state(),
                command.zipCode(),
                command.country()
        );

        Order order = new Order(
                null,
                null,
                userId,
                orderItems,
                0,
                OrderStatus.PENDING,
                shippingAddress,
                null,
                null
        );

        Order savedOrder = orderRepositoryPort.save(order);
        cartPort.clearCart(userId);
        
        return savedOrder;
    }

    private OrderItem mapToOrderItem(CartItem cartItem) {
        return new OrderItem(
                null,
                cartItem.getProduct().getId(),
                cartItem.getProduct().getName(),
                cartItem.getQuantity(),
                cartItem.getProduct().getPrice()
        );
    }

    @Override
    public Order getOrderById(Long orderId) {
        return orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    @Override
    public Order getOrderByNumber(String orderNumber) {
        return orderRepositoryPort.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderNumber));
    }

    @Override
    public List<Order> getUserOrders(Long userId) {
        return orderRepositoryPort.findByUserId(userId);
    }

    @Override
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = getOrderById(orderId);
        order.updateStatus(newStatus);
        return orderRepositoryPort.save(order);
    }
}

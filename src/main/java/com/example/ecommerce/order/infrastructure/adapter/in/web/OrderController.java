package com.example.ecommerce.order.infrastructure.adapter.in.web;

import com.example.ecommerce.notification.application.port.in.SendNotificationUseCase;
import com.example.ecommerce.order.application.port.in.*;
import com.example.ecommerce.order.domain.model.Order;
import com.example.ecommerce.order.domain.model.OrderStatus;
import com.example.ecommerce.order.infrastructure.adapter.in.web.dto.CreateOrderRequest;
import com.example.ecommerce.order.infrastructure.adapter.in.web.dto.OrderResponse;
import com.example.ecommerce.order.infrastructure.adapter.in.web.dto.UpdateOrderStatusRequest;
import com.example.ecommerce.user.application.port.out.UserRepositoryPort;
import com.example.ecommerce.user.domain.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final GetUserOrdersUseCase getUserOrdersUseCase;
    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;
    private final SendNotificationUseCase sendNotificationUseCase;
    private final UserRepositoryPort userRepositoryPort;

    public OrderController(
            CreateOrderUseCase createOrderUseCase,
            GetOrderUseCase getOrderUseCase,
            GetUserOrdersUseCase getUserOrdersUseCase,
            UpdateOrderStatusUseCase updateOrderStatusUseCase,
            SendNotificationUseCase sendNotificationUseCase,
            UserRepositoryPort userRepositoryPort) {
        this.createOrderUseCase = createOrderUseCase;
        this.getOrderUseCase = getOrderUseCase;
        this.getUserOrdersUseCase = getUserOrdersUseCase;
        this.updateOrderStatusUseCase = updateOrderStatusUseCase;
        this.sendNotificationUseCase = sendNotificationUseCase;
        this.userRepositoryPort = userRepositoryPort;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody CreateOrderRequest request) {
        
        // Map frontend request to domain command
        CreateOrderRequest.ShippingAddressRequest shipping = request.getShipping();
        CreateOrderUseCase.CreateOrderCommand command = new CreateOrderUseCase.CreateOrderCommand(
                shipping != null ? shipping.getAddress() : null,
                shipping != null ? shipping.getRegion() : null,
                shipping != null ? shipping.getDocumentType() : null,
                shipping != null ? shipping.getDocumentNumber() : null,
                shipping != null ? shipping.getRegion() : null,
                request.getDiscountCode()
        );
        
        Order order = createOrderUseCase.createOrder(userId, command);

        userRepositoryPort.findById(userId).ifPresent(user ->
                sendNotificationUseCase.sendOrderConfirmation(
                        userId, user.getEmail(), order.getOrderNumber(), order.getTotalAmount())
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.fromDomain(order));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        Order order = getOrderUseCase.getOrderById(id);
        return ResponseEntity.ok(OrderResponse.fromDomain(order));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getUserOrders(@RequestHeader("X-User-Id") Long userId) {
        List<Order> orders = getUserOrdersUseCase.getUserOrders(userId);
        List<OrderResponse> responses = orders.stream()
                .map(OrderResponse::fromDomain)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody UpdateOrderStatusRequest request) {
        Order order = updateOrderStatusUseCase.updateOrderStatus(id, request.getStatus());

        if (order.getStatus() == OrderStatus.SHIPPED) {
            userRepositoryPort.findById(order.getUserId()).ifPresent(user ->
                    sendNotificationUseCase.sendOrderShipped(
                            order.getUserId(), user.getEmail(), order.getOrderNumber())
            );
        }

        return ResponseEntity.ok(OrderResponse.fromDomain(order));
    }
}

package com.example.ecommerce.order.application.service;

import com.example.ecommerce.cart.domain.model.Cart;
import com.example.ecommerce.cart.domain.model.CartItem;
import com.example.ecommerce.order.application.port.in.CreateOrderUseCase;
import com.example.ecommerce.order.application.port.out.CartPort;
import com.example.ecommerce.order.application.port.out.OrderRepositoryPort;
import com.example.ecommerce.order.domain.exception.DirectOrderPaidTransitionNotAllowedException;
import com.example.ecommerce.order.domain.exception.OrderNotFoundException;
import com.example.ecommerce.order.domain.model.Order;
import com.example.ecommerce.order.domain.model.OrderItem;
import com.example.ecommerce.order.domain.model.OrderStatus;
import com.example.ecommerce.order.domain.model.ShippingAddress;
import com.example.ecommerce.product.domain.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepositoryPort orderRepositoryPort;

    @Mock
    private CartPort cartPort;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepositoryPort, cartPort);
    }

    @Test
    void shouldCreateOrderSuccessfully() {
        Product product = new Product(1L, "Product 1", "Description", 10.0);
        CartItem cartItem = new CartItem(1L, product, 2);
        Cart cart = new Cart(1L, 1L, Arrays.asList(cartItem));

        OrderItem orderItem = new OrderItem(1L, 1L, "Product 1", 2, 10.0);
        ShippingAddress address = new ShippingAddress("123 Main St", "City", "State", "12345", "Country");
        Order savedOrder = new Order(
                1L,
                "ORD-12345678",
                1L,
                Arrays.asList(orderItem),
                20.0,
                OrderStatus.PENDING,
                address,
                null,
                null
        );

        when(cartPort.getCartByUserId(1L)).thenReturn(cart);
        when(orderRepositoryPort.save(any(Order.class))).thenReturn(savedOrder);

        CreateOrderUseCase.CreateOrderCommand command = new CreateOrderUseCase.CreateOrderCommand(
                "123 Main St",
                "City",
                "State",
                "12345",
                "Country"
        );

        Order result = orderService.createOrder(1L, command);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("ORD-12345678", result.getOrderNumber());
        verify(cartPort).clearCart(1L);
    }

    @Test
    void shouldThrowExceptionWhenCartIsEmpty() {
        Cart emptyCart = new Cart(1L, 1L);

        when(cartPort.getCartByUserId(1L)).thenReturn(emptyCart);

        CreateOrderUseCase.CreateOrderCommand command = new CreateOrderUseCase.CreateOrderCommand(
                "123 Main St",
                "City",
                "State",
                "12345",
                "Country"
        );

        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(1L, command));
    }

    @Test
    void shouldGetOrderById() {
        OrderItem orderItem = new OrderItem(1L, 1L, "Product 1", 2, 10.0);
        ShippingAddress address = new ShippingAddress("123 Main St", "City", "State", "12345", "Country");
        Order order = new Order(
                1L,
                "ORD-12345678",
                1L,
                Arrays.asList(orderItem),
                20.0,
                OrderStatus.PENDING,
                address,
                null,
                null
        );

        when(orderRepositoryPort.findById(1L)).thenReturn(Optional.of(order));

        Order result = orderService.getOrderById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void shouldThrowExceptionWhenOrderNotFound() {
        when(orderRepositoryPort.findById(1L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.getOrderById(1L));
    }

    @Test
    void shouldGetUserOrders() {
        OrderItem orderItem = new OrderItem(1L, 1L, "Product 1", 2, 10.0);
        ShippingAddress address = new ShippingAddress("123 Main St", "City", "State", "12345", "Country");
        Order order1 = new Order(
                1L,
                "ORD-12345678",
                1L,
                Arrays.asList(orderItem),
                20.0,
                OrderStatus.PENDING,
                address,
                null,
                null
        );
        Order order2 = new Order(
                2L,
                "ORD-87654321",
                1L,
                Arrays.asList(orderItem),
                30.0,
                OrderStatus.PAID,
                address,
                null,
                null
        );

        when(orderRepositoryPort.findByUserId(1L)).thenReturn(Arrays.asList(order1, order2));

        List<Order> results = orderService.getUserOrders(1L);

        assertEquals(2, results.size());
    }

    @Test
    void shouldUpdateOrderStatusForAllowedNonPaymentTransition() {
        OrderItem orderItem = new OrderItem(1L, 1L, "Product 1", 2, 10.0);
        ShippingAddress address = new ShippingAddress("123 Main St", "City", "State", "12345", "Country");
        Order order = new Order(
                1L,
                "ORD-12345678",
                1L,
                Arrays.asList(orderItem),
                20.0,
                OrderStatus.PENDING,
                address,
                null,
                null
        );

        when(orderRepositoryPort.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepositoryPort.save(any(Order.class))).thenReturn(order);

        Order result = orderService.updateOrderStatus(1L, OrderStatus.CANCELLED);

        assertEquals(OrderStatus.CANCELLED, result.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingOrderStatusDirectlyToPaid() {
        assertThrows(DirectOrderPaidTransitionNotAllowedException.class,
                () -> orderService.updateOrderStatus(1L, OrderStatus.PAID));

        verify(orderRepositoryPort, never()).findById(any(Long.class));
        verify(orderRepositoryPort, never()).save(any(Order.class));
    }
}

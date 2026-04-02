package com.example.ecommerce.payment.infrastructure.adapter.out.order;

import com.example.ecommerce.order.application.port.out.OrderRepositoryPort;
import com.example.ecommerce.order.domain.model.Order;
import com.example.ecommerce.order.domain.model.OrderItem;
import com.example.ecommerce.order.domain.model.OrderStatus;
import com.example.ecommerce.order.domain.model.ShippingAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderPaymentTransitionAdapterTest {

    @Mock
    private OrderRepositoryPort orderRepositoryPort;

    private OrderPaymentTransitionAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new OrderPaymentTransitionAdapter(orderRepositoryPort);
    }

    @Test
    void shouldMarkOrderAsPaidWhenPaymentCaptured() {
        Order order = order(OrderStatus.PENDING);
        when(orderRepositoryPort.findById(10L)).thenReturn(Optional.of(order));

        adapter.markPaidAfterPayment(10L, 99L);

        verify(orderRepositoryPort).save(order);
    }

    @Test
    void shouldNotSaveWhenOrderAlreadyPaid() {
        Order order = order(OrderStatus.PAID);
        when(orderRepositoryPort.findById(10L)).thenReturn(Optional.of(order));

        adapter.markPaidAfterPayment(10L, 99L);

        verify(orderRepositoryPort, never()).save(order);
    }

    private Order order(OrderStatus status) {
        return new Order(
                10L,
                "ORD-10000001",
                7L,
                List.of(new OrderItem(1L, 20L, "Book", 1, 10.0)),
                10.0,
                status,
                new ShippingAddress("Street", "City", "State", "12345", "Country"),
                null,
                null
        );
    }
}

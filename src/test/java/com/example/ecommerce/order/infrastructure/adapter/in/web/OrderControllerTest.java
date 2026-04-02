package com.example.ecommerce.order.infrastructure.adapter.in.web;

import com.example.ecommerce.order.application.port.in.CreateOrderUseCase;
import com.example.ecommerce.order.application.port.in.GetOrderUseCase;
import com.example.ecommerce.order.application.port.in.GetUserOrdersUseCase;
import com.example.ecommerce.order.application.port.in.UpdateOrderStatusUseCase;
import com.example.ecommerce.order.domain.exception.DirectOrderPaidTransitionNotAllowedException;
import com.example.ecommerce.order.domain.model.Order;
import com.example.ecommerce.order.domain.model.OrderItem;
import com.example.ecommerce.order.domain.model.OrderStatus;
import com.example.ecommerce.order.domain.model.ShippingAddress;
import com.example.ecommerce.product.infrastructure.adapter.in.web.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CreateOrderUseCase createOrderUseCase;

    @Mock
    private GetOrderUseCase getOrderUseCase;

    @Mock
    private GetUserOrdersUseCase getUserOrdersUseCase;

    @Mock
    private UpdateOrderStatusUseCase updateOrderStatusUseCase;

    @BeforeEach
    void setUp() {
        OrderController orderController = new OrderController(
                createOrderUseCase,
                getOrderUseCase,
                getUserOrdersUseCase,
                updateOrderStatusUseCase
        );

        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldBlockDirectPendingToPaidStatusUpdateAndGuidePaymentFlow() throws Exception {
        when(updateOrderStatusUseCase.updateOrderStatus(eq(1L), eq(OrderStatus.PAID)))
                .thenThrow(new DirectOrderPaidTransitionNotAllowedException());

        mockMvc.perform(put("/api/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PAID\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ORDER_STATUS_PAYMENT_FLOW_REQUIRED"))
                .andExpect(jsonPath("$.message").value("Direct order status update to PAID is not allowed. Use POST /api/payments to complete payment."))
                .andExpect(jsonPath("$.retryable").value(false))
                .andExpect(jsonPath("$.path").value("/api/orders/1/status"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void shouldAllowNonPaymentStatusTransitionViaOrderStatusEndpoint() throws Exception {
        Order order = new Order(
                1L,
                "ORD-12345678",
                10L,
                List.of(new OrderItem(1L, 100L, "Product", 1, 20.0)),
                20.0,
                OrderStatus.CANCELLED,
                new ShippingAddress("123 Main St", "City", "State", "12345", "Country"),
                null,
                null
        );

        when(updateOrderStatusUseCase.updateOrderStatus(eq(1L), eq(OrderStatus.CANCELLED))).thenReturn(order);

        mockMvc.perform(put("/api/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CANCELLED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}

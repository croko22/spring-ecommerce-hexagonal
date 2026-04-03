package com.example.ecommerce.payment.infrastructure.adapter.in.web;

import com.example.ecommerce.order.domain.model.OrderStatus;
import com.example.ecommerce.order.infrastructure.adapter.out.persistence.OrderEntity;
import com.example.ecommerce.order.infrastructure.adapter.out.persistence.OrderItemEntity;
import com.example.ecommerce.order.infrastructure.adapter.out.persistence.OrderJpaRepository;
import com.example.ecommerce.order.infrastructure.adapter.out.persistence.ShippingAddressEmbeddable;
import com.example.ecommerce.payment.application.port.out.OrderPaymentTransitionPort;
import com.example.ecommerce.payment.application.port.out.PaymentProviderPort;
import com.example.ecommerce.payment.domain.model.PaymentOperation;
import com.example.ecommerce.payment.infrastructure.adapter.out.persistence.PaymentIdempotencyEntity;
import com.example.ecommerce.payment.infrastructure.adapter.out.persistence.PaymentIdempotencyJpaRepository;
import com.example.ecommerce.payment.infrastructure.adapter.out.persistence.PaymentJpaRepository;
import com.example.ecommerce.shared.infrastructure.PostgresContainerIntegrationTest;
import com.example.ecommerce.user.application.port.out.JWTProviderPort;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "feature.payment.enabled=true")
@AutoConfigureMockMvc
@Transactional
@Tag("integration")
class PaymentHardeningIntegrationTest extends PostgresContainerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderJpaRepository orderJpaRepository;

    @Autowired
    private PaymentIdempotencyJpaRepository paymentIdempotencyJpaRepository;

    @Autowired
    private PaymentJpaRepository paymentJpaRepository;

    @MockitoBean
    private JWTProviderPort jwtProviderPort;

    @MockitoBean
    private PaymentProviderPort paymentProviderPort;

    @MockitoBean
    private OrderPaymentTransitionPort orderPaymentTransitionPort;

    @Test
    void shouldReplayPersistedResultForDuplicateRequestAndCallProviderOnlyOnce() throws Exception {
        Long orderId = createPendingOrder(42L);
        String idempotencyKey = "idem-hardening-replay";
        AtomicInteger providerCalls = new AtomicInteger(0);

        mockAuthenticatedUser(42L, "john.doe@example.com");
        when(paymentProviderPort.authorizeAndCapture(anyLong(), anyLong(), anyString()))
                .thenAnswer(invocation -> {
                    providerCalls.incrementAndGet();
                    Optional<PaymentIdempotencyEntity> reservation =
                            paymentIdempotencyJpaRepository.findByOperationAndActorScopeAndIdempotencyKey(
                                    PaymentOperation.INITIATE,
                                    "42:" + orderId,
                                    idempotencyKey
                            );
                    assertTrue(reservation.isPresent());
                    assertEquals(PaymentIdempotencyEntity.ProcessingStatus.IN_PROGRESS, reservation.get().getStatus());
                    assertTrue(reservation.get().getResponseStatus() == null || reservation.get().getResponseStatus().isBlank());
                    return new PaymentProviderPort.ProviderAuthorizeCaptureResult(true, "prov-fixed-777", null);
                });

        String body = "{" +
                "\"orderId\":" + orderId + "," +
                "\"idempotencyKey\":\"" + idempotencyKey + "\"," +
                "\"paymentMethodToken\":\"pm-ok\"}";

        String firstResponse = mockMvc.perform(post("/api/payments")
                        .header("Authorization", "Bearer valid-token")
                        .header("X-User-Id", "999")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CAPTURED"))
                .andExpect(jsonPath("$.providerReference").value("prov-fixed-777"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long firstPaymentId = extractLongField(firstResponse, "paymentId");

        mockMvc.perform(post("/api/payments")
                        .header("Authorization", "Bearer valid-token")
                        .header("X-User-Id", "1")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentId").value(firstPaymentId))
                .andExpect(jsonPath("$.status").value("CAPTURED"))
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.providerReference").value("prov-fixed-777"));

        verify(paymentProviderPort, times(1)).authorizeAndCapture(orderId, 42L, "pm-ok");
        assertEquals(1, providerCalls.get());
        assertEquals(1L, paymentJpaRepository.count());

        Optional<PaymentIdempotencyEntity> stored = paymentIdempotencyJpaRepository.findByOperationAndActorScopeAndIdempotencyKey(
                PaymentOperation.INITIATE,
                "42:" + orderId,
                idempotencyKey
        );
        assertTrue(stored.isPresent());
        assertEquals(PaymentIdempotencyEntity.ProcessingStatus.COMPLETED, stored.get().getStatus());
        assertEquals("CAPTURED", stored.get().getResponseStatus());
        assertNotNull(stored.get().getResponseBody());
        assertFalse(stored.get().getResponseBody().isBlank());
    }

    @Test
    void shouldReturnForbiddenForNonOwnerEvenWithSpoofedHeader() throws Exception {
        Long orderId = createPendingOrder(99L);
        mockAuthenticatedUser(42L, "john.doe@example.com");

        mockMvc.perform(post("/api/payments")
                        .header("Authorization", "Bearer valid-token")
                        .header("X-User-Id", "99")
                        .contentType("application/json")
                        .content("{" +
                                "\"orderId\":" + orderId + "," +
                                "\"idempotencyKey\":\"idem-owner-mismatch\"," +
                                "\"paymentMethodToken\":\"pm-ok\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PAYMENT_FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("Access denied to payment resource"))
                .andExpect(jsonPath("$.retryable").value(false));
    }

    private void mockAuthenticatedUser(Long userId, String email) {
        when(jwtProviderPort.validateToken("valid-token")).thenReturn(true);
        when(jwtProviderPort.getEmailFromToken("valid-token")).thenReturn(email);
        when(jwtProviderPort.getUserIdFromToken("valid-token")).thenReturn(userId);
    }

    private Long createPendingOrder(Long userId) {
        OrderEntity order = new OrderEntity();
        order.setOrderNumber("ORD-HARD-" + System.nanoTime());
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(50.0);
        order.setShippingAddress(new ShippingAddressEmbeddable("Street", "City", "State", "12345", "Country"));
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        OrderItemEntity item = new OrderItemEntity();
        item.setProductId(1L);
        item.setProductName("Test Product");
        item.setQuantity(1);
        item.setUnitPrice(50.0);
        item.setSubtotal(50.0);
        item.setOrder(order);
        order.setItems(java.util.List.of(item));

        return orderJpaRepository.save(order).getId();
    }

    private long extractLongField(String json, String fieldName) {
        String marker = "\"" + fieldName + "\":";
        int start = json.indexOf(marker);
        if (start < 0) {
            throw new IllegalStateException("Field not found in json response: " + fieldName);
        }
        int valueStart = start + marker.length();
        int valueEnd = valueStart;
        while (valueEnd < json.length() && Character.isDigit(json.charAt(valueEnd))) {
            valueEnd++;
        }
        return Long.parseLong(json.substring(valueStart, valueEnd));
    }
}

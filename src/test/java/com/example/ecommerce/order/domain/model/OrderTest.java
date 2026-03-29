package com.example.ecommerce.order.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    @Test
    void shouldCreateOrderWithValidData() {
        OrderItem item = new OrderItem(1L, 1L, "Product 1", 2, 10.0);
        ShippingAddress address = new ShippingAddress("123 Main St", "City", "State", "12345", "Country");

        Order order = new Order(
                1L,
                "ORD-12345678",
                1L,
                Arrays.asList(item),
                20.0,
                OrderStatus.PENDING,
                address,
                null,
                null
        );

        assertEquals(1L, order.getId());
        assertEquals("ORD-12345678", order.getOrderNumber());
        assertEquals(1L, order.getUserId());
        assertEquals(1, order.getItems().size());
        assertEquals(20.0, order.getTotalAmount());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertNotNull(order.getCreatedAt());
    }

    @Test
    void shouldGenerateOrderNumberWhenNotProvided() {
        OrderItem item = new OrderItem(1L, 1L, "Product 1", 2, 10.0);
        ShippingAddress address = new ShippingAddress("123 Main St", "City", "State", "12345", "Country");

        Order order = new Order(
                null,
                null,
                1L,
                Arrays.asList(item),
                20.0,
                null,
                address,
                null,
                null
        );

        assertNotNull(order.getOrderNumber());
        assertTrue(order.getOrderNumber().startsWith("ORD-"));
    }

    @Test
    void shouldThrowExceptionWhenUserIdIsNull() {
        OrderItem item = new OrderItem(1L, 1L, "Product 1", 2, 10.0);
        ShippingAddress address = new ShippingAddress("123 Main St", "City", "State", "12345", "Country");

        Executable executable = () -> new Order(
                1L,
                "ORD-12345678",
                null,
                Arrays.asList(item),
                20.0,
                OrderStatus.PENDING,
                address,
                null,
                null
        );

        assertThrows(IllegalArgumentException.class, executable, "User ID cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenItemsAreEmpty() {
        ShippingAddress address = new ShippingAddress("123 Main St", "City", "State", "12345", "Country");

        Executable executable = () -> new Order(
                1L,
                "ORD-12345678",
                1L,
                Collections.emptyList(),
                0.0,
                OrderStatus.PENDING,
                address,
                null,
                null
        );

        assertThrows(IllegalArgumentException.class, executable, "Order must have at least one item");
    }

    @Test
    void shouldThrowExceptionWhenShippingAddressIsNull() {
        OrderItem item = new OrderItem(1L, 1L, "Product 1", 2, 10.0);

        Executable executable = () -> new Order(
                1L,
                "ORD-12345678",
                1L,
                Arrays.asList(item),
                20.0,
                OrderStatus.PENDING,
                null,
                null,
                null
        );

        assertThrows(IllegalArgumentException.class, executable, "Shipping address cannot be null");
    }

    @Test
    void shouldUpdateStatusFromPendingToPaid() {
        OrderItem item = new OrderItem(1L, 1L, "Product 1", 2, 10.0);
        ShippingAddress address = new ShippingAddress("123 Main St", "City", "State", "12345", "Country");

        Order order = new Order(
                1L,
                "ORD-12345678",
                1L,
                Arrays.asList(item),
                20.0,
                OrderStatus.PENDING,
                address,
                null,
                null
        );

        order.updateStatus(OrderStatus.PAID);

        assertEquals(OrderStatus.PAID, order.getStatus());
    }

    @Test
    void shouldUpdateStatusFromPaidToShipped() {
        OrderItem item = new OrderItem(1L, 1L, "Product 1", 2, 10.0);
        ShippingAddress address = new ShippingAddress("123 Main St", "City", "State", "12345", "Country");

        Order order = new Order(
                1L,
                "ORD-12345678",
                1L,
                Arrays.asList(item),
                20.0,
                OrderStatus.PAID,
                address,
                null,
                null
        );

        order.updateStatus(OrderStatus.SHIPPED);

        assertEquals(OrderStatus.SHIPPED, order.getStatus());
    }

    @Test
    void shouldThrowExceptionForInvalidStatusTransition() {
        OrderItem item = new OrderItem(1L, 1L, "Product 1", 2, 10.0);
        ShippingAddress address = new ShippingAddress("123 Main St", "City", "State", "12345", "Country");

        Order order = new Order(
                1L,
                "ORD-12345678",
                1L,
                Arrays.asList(item),
                20.0,
                OrderStatus.PENDING,
                address,
                null,
                null
        );

        Executable executable = () -> order.updateStatus(OrderStatus.DELIVERED);

        assertThrows(IllegalArgumentException.class, executable);
    }

    @Test
    void shouldThrowExceptionWhenCancelingDeliveredOrder() {
        OrderItem item = new OrderItem(1L, 1L, "Product 1", 2, 10.0);
        ShippingAddress address = new ShippingAddress("123 Main St", "City", "State", "12345", "Country");

        Order order = new Order(
                1L,
                "ORD-12345678",
                1L,
                Arrays.asList(item),
                20.0,
                OrderStatus.DELIVERED,
                address,
                null,
                null
        );

        Executable executable = () -> order.updateStatus(OrderStatus.CANCELLED);

        assertThrows(IllegalArgumentException.class, executable);
    }

    @Test
    void shouldCreateOrderItemWithValidData() {
        OrderItem item = new OrderItem(1L, 1L, "Product 1", 2, 10.0);

        assertEquals(1L, item.getId());
        assertEquals(1L, item.getProductId());
        assertEquals("Product 1", item.getProductName());
        assertEquals(2, item.getQuantity());
        assertEquals(10.0, item.getUnitPrice());
        assertEquals(20.0, item.getSubtotal());
    }

    @Test
    void shouldThrowExceptionWhenOrderItemQuantityIsZero() {
        Executable executable = () -> new OrderItem(1L, 1L, "Product 1", 0, 10.0);

        assertThrows(IllegalArgumentException.class, executable, "Quantity must be greater than 0");
    }

    @Test
    void shouldCreateShippingAddressWithValidData() {
        ShippingAddress address = new ShippingAddress("123 Main St", "City", "State", "12345", "Country");

        assertEquals("123 Main St", address.getStreet());
        assertEquals("City", address.getCity());
        assertEquals("State", address.getState());
        assertEquals("12345", address.getZipCode());
        assertEquals("Country", address.getCountry());
    }

    @Test
    void shouldThrowExceptionWhenStreetIsBlank() {
        Executable executable = () -> new ShippingAddress("", "City", "State", "12345", "Country");

        assertThrows(IllegalArgumentException.class, executable, "Street cannot be blank");
    }

    @Test
    void shouldThrowExceptionWhenCityIsBlank() {
        Executable executable = () -> new ShippingAddress("123 Main St", "", "State", "12345", "Country");

        assertThrows(IllegalArgumentException.class, executable, "City cannot be blank");
    }
}

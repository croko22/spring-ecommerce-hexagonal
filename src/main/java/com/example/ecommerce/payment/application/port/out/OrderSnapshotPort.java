package com.example.ecommerce.payment.application.port.out;

public interface OrderSnapshotPort {

    OrderSnapshot getOrderSnapshot(Long orderId);

    record OrderSnapshot(
            Long orderId,
            Long userId,
            String status,
            double totalAmount,
            String currency
    ) {
    }
}

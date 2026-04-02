package com.example.ecommerce.payment.domain.model;

import com.example.ecommerce.payment.domain.exception.InvalidPaymentTransitionException;
import com.example.ecommerce.payment.domain.exception.UnsupportedPaymentOperationException;

import java.time.LocalDateTime;

public class Payment {

    private Long id;
    private Long orderId;
    private Long userId;
    private Money amount;
    private PaymentStatus status;
    private ProviderReference providerReference;
    private String failureCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Payment(
            Long id,
            Long orderId,
            Long userId,
            Money amount,
            PaymentStatus status,
            ProviderReference providerReference,
            String failureCode,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }

        this.id = id;
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.status = status != null ? status : PaymentStatus.INITIATED;
        this.providerReference = providerReference;
        this.failureCode = failureCode;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public static Payment initiate(Long orderId, Long userId, Money amount) {
        return new Payment(null, orderId, userId, amount, PaymentStatus.INITIATED, null, null, null, null);
    }

    public void authorize(ProviderReference providerReference) {
        assertNotTerminal();
        if (status != PaymentStatus.INITIATED) {
            throw new InvalidPaymentTransitionException("Cannot authorize payment from status " + status);
        }
        this.providerReference = providerReference;
        this.failureCode = null;
        transitionTo(PaymentStatus.AUTHORIZED);
    }

    public void capture(ProviderReference providerReference) {
        assertNotTerminal();
        if (status != PaymentStatus.AUTHORIZED) {
            throw new InvalidPaymentTransitionException("Cannot capture payment from status " + status);
        }
        this.providerReference = providerReference;
        this.failureCode = null;
        transitionTo(PaymentStatus.CAPTURED);
    }

    public void fail(String failureCode) {
        assertNotTerminal();
        if (status != PaymentStatus.INITIATED && status != PaymentStatus.AUTHORIZED) {
            throw new InvalidPaymentTransitionException("Cannot fail payment from status " + status);
        }
        this.failureCode = failureCode;
        transitionTo(PaymentStatus.FAILED);
    }

    public void cancel(String failureCode) {
        assertNotTerminal();
        if (status != PaymentStatus.INITIATED && status != PaymentStatus.AUTHORIZED) {
            throw new InvalidPaymentTransitionException("Cannot cancel payment from status " + status);
        }
        this.failureCode = failureCode;
        transitionTo(PaymentStatus.CANCELLED);
    }

    public void refund() {
        throw new UnsupportedPaymentOperationException("Refund is not supported in MVP");
    }

    private void transitionTo(PaymentStatus nextStatus) {
        if (nextStatus == PaymentStatus.REFUNDED) {
            throw new UnsupportedPaymentOperationException("Refunded transition is not supported in MVP");
        }
        this.status = nextStatus;
        this.updatedAt = LocalDateTime.now();
    }

    private void assertNotTerminal() {
        if (status == PaymentStatus.CAPTURED || status == PaymentStatus.FAILED || status == PaymentStatus.CANCELLED) {
            throw new InvalidPaymentTransitionException("Cannot transition terminal payment status " + status);
        }
        if (status == PaymentStatus.REFUNDED) {
            throw new UnsupportedPaymentOperationException("Refunded transition is not supported in MVP");
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public Money getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public ProviderReference getProviderReference() {
        return providerReference;
    }

    public String getFailureCode() {
        return failureCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}

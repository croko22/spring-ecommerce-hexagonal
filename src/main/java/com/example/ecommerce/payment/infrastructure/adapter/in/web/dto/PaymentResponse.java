package com.example.ecommerce.payment.infrastructure.adapter.in.web.dto;

import com.example.ecommerce.payment.application.port.in.GetPaymentUseCase;
import com.example.ecommerce.payment.application.port.in.InitiatePaymentUseCase;

import java.math.BigDecimal;

public class PaymentResponse {

    private Long paymentId;
    private String status;
    private BigDecimal amount;
    private String currency;
    private Long orderId;
    private String providerReference;

    public static PaymentResponse fromInitiateResult(InitiatePaymentUseCase.InitiatePaymentResult result) {
        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(result.paymentId());
        response.setStatus(result.status());
        response.setAmount(result.amount());
        response.setCurrency(result.currency());
        response.setOrderId(result.orderId());
        response.setProviderReference(result.providerReference());
        return response;
    }

    public static PaymentResponse fromDetails(GetPaymentUseCase.PaymentDetails details) {
        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(details.paymentId());
        response.setStatus(details.status());
        response.setAmount(details.amount());
        response.setCurrency(details.currency());
        response.setOrderId(details.orderId());
        response.setProviderReference(details.providerReference());
        return response;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getProviderReference() {
        return providerReference;
    }

    public void setProviderReference(String providerReference) {
        this.providerReference = providerReference;
    }
}

package com.example.ecommerce.payment.infrastructure.adapter.in.web.dto;

import com.example.ecommerce.payment.application.port.in.HandlePaymentWebhookUseCase;

public class PaymentWebhookReceiptResponse {

    private Long receiptId;
    private String providerEventId;
    private String ingestStatus;
    private String reconciliationStatus;

    public static PaymentWebhookReceiptResponse fromResult(HandlePaymentWebhookUseCase.HandlePaymentWebhookResult result) {
        PaymentWebhookReceiptResponse response = new PaymentWebhookReceiptResponse();
        response.setReceiptId(result.receiptId());
        response.setProviderEventId(result.providerEventId());
        response.setIngestStatus(result.ingestStatus());
        response.setReconciliationStatus(result.reconciliationStatus());
        return response;
    }

    public Long getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(Long receiptId) {
        this.receiptId = receiptId;
    }

    public String getProviderEventId() {
        return providerEventId;
    }

    public void setProviderEventId(String providerEventId) {
        this.providerEventId = providerEventId;
    }

    public String getIngestStatus() {
        return ingestStatus;
    }

    public void setIngestStatus(String ingestStatus) {
        this.ingestStatus = ingestStatus;
    }

    public String getReconciliationStatus() {
        return reconciliationStatus;
    }

    public void setReconciliationStatus(String reconciliationStatus) {
        this.reconciliationStatus = reconciliationStatus;
    }
}

package com.example.ecommerce.payment.infrastructure.adapter.in.web.dto;

import java.time.Instant;

public class PaymentErrorResponse {

    private String code;
    private String message;
    private boolean retryable;
    private String path;
    private Instant timestamp;

    public PaymentErrorResponse() {
    }

    public PaymentErrorResponse(String code, String message, boolean retryable, String path, Instant timestamp) {
        this.code = code;
        this.message = message;
        this.retryable = retryable;
        this.path = path;
        this.timestamp = timestamp;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRetryable() {
        return retryable;
    }

    public void setRetryable(boolean retryable) {
        this.retryable = retryable;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}

package com.example.ecommerce.payment.infrastructure.adapter.in.web;

import com.example.ecommerce.payment.application.exception.IdempotencyConflictException;
import com.example.ecommerce.payment.application.exception.OrderNotPayableException;
import com.example.ecommerce.payment.application.exception.PaymentAccessDeniedException;
import com.example.ecommerce.payment.application.exception.PaymentFeatureDisabledException;
import com.example.ecommerce.payment.application.exception.PaymentNotFoundException;
import com.example.ecommerce.payment.application.exception.PaymentWebhookSignatureInvalidException;
import com.example.ecommerce.payment.application.exception.ProviderTimeoutException;
import com.example.ecommerce.payment.application.exception.ProviderUnavailableException;
import com.example.ecommerce.payment.domain.exception.InvalidPaymentTransitionException;
import com.example.ecommerce.payment.domain.exception.UnsupportedPaymentOperationException;
import com.example.ecommerce.payment.infrastructure.adapter.in.web.dto.PaymentErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

@ControllerAdvice(basePackages = "com.example.ecommerce.payment.infrastructure.adapter.in.web")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PaymentExceptionHandler {

    @ExceptionHandler({
            InvalidPaymentTransitionException.class,
            UnsupportedPaymentOperationException.class,
            OrderNotPayableException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<PaymentErrorResponse> handleDomainValidation(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        return buildResponse("PAYMENT_DOMAIN_ERROR", ex.getMessage(), false, request.getRequestURI(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IdempotencyConflictException.class)
    public ResponseEntity<PaymentErrorResponse> handleIdempotencyConflict(
            IdempotencyConflictException ex,
            HttpServletRequest request
    ) {
        return buildResponse("PAYMENT_IDEMPOTENCY_CONFLICT", ex.getMessage(), false, request.getRequestURI(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<PaymentErrorResponse> handleNotFound(
            PaymentNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildResponse("PAYMENT_NOT_FOUND", ex.getMessage(), false, request.getRequestURI(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PaymentAccessDeniedException.class)
    public ResponseEntity<PaymentErrorResponse> handleForbidden(
            PaymentAccessDeniedException ex,
            HttpServletRequest request
    ) {
        return buildResponse("PAYMENT_FORBIDDEN", ex.getMessage(), false, request.getRequestURI(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(PaymentWebhookSignatureInvalidException.class)
    public ResponseEntity<PaymentErrorResponse> handleWebhookSignatureInvalid(
            PaymentWebhookSignatureInvalidException ex,
            HttpServletRequest request
    ) {
        return buildResponse("PAYMENT_WEBHOOK_SIGNATURE_INVALID", ex.getMessage(), false, request.getRequestURI(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(PaymentFeatureDisabledException.class)
    public ResponseEntity<PaymentErrorResponse> handleFeatureDisabled(
            PaymentFeatureDisabledException ex,
            HttpServletRequest request
    ) {
        return buildResponse("PAYMENT_FEATURE_DISABLED", ex.getMessage(), true, request.getRequestURI(), HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(ProviderTimeoutException.class)
    public ResponseEntity<PaymentErrorResponse> handleProviderTimeout(
            ProviderTimeoutException ex,
            HttpServletRequest request
    ) {
        return buildResponse("PAYMENT_PROVIDER_TIMEOUT", ex.getMessage(), true, request.getRequestURI(), HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(ProviderUnavailableException.class)
    public ResponseEntity<PaymentErrorResponse> handleProviderUnavailable(
            ProviderUnavailableException ex,
            HttpServletRequest request
    ) {
        return buildResponse("PAYMENT_PROVIDER_UNAVAILABLE", ex.getMessage(), true, request.getRequestURI(), HttpStatus.SERVICE_UNAVAILABLE);
    }

    private ResponseEntity<PaymentErrorResponse> buildResponse(
            String code,
            String message,
            boolean retryable,
            String path,
            HttpStatus status
    ) {
        PaymentErrorResponse response = new PaymentErrorResponse(
                code,
                message,
                retryable,
                path,
                Instant.now()
        );
        return ResponseEntity.status(status).body(response);
    }
}

package com.example.ecommerce.idempotency.infrastructure.adapter.in.web;

import com.example.ecommerce.idempotency.application.port.out.IdempotencyKeyRepositoryPort;
import com.example.ecommerce.idempotency.domain.model.IdempotencyKey;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
@Order(1)
public class IdempotencyFilter implements Filter {

    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    private static final String IDEMPOTENCY_REPLAY_HEADER = "X-Idempotency-Replayed";

    private final IdempotencyKeyRepositoryPort idempotencyKeyRepositoryPort;

    public IdempotencyFilter(IdempotencyKeyRepositoryPort idempotencyKeyRepositoryPort) {
        this.idempotencyKeyRepositoryPort = idempotencyKeyRepositoryPort;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (!"POST".equalsIgnoreCase(httpRequest.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String idempotencyKeyValue = httpRequest.getHeader(IDEMPOTENCY_KEY_HEADER);
        String resourcePath = httpRequest.getRequestURI();

        if (!isIdempotentPath(resourcePath)) {
            chain.doFilter(request, response);
            return;
        }

        if (idempotencyKeyValue == null || idempotencyKeyValue.isBlank()) {
            httpResponse.sendError(HttpStatus.BAD_REQUEST.value(), "Idempotency-Key header is required for POST requests");
            return;
        }

        IdempotencyKey idempotencyKey;
        try {
            idempotencyKey = new IdempotencyKey(idempotencyKeyValue);
        } catch (IllegalArgumentException e) {
            httpResponse.sendError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
            return;
        }

        String requestBody = new String(httpRequest.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String requestHash = hashRequest(requestBody);

        var existing = idempotencyKeyRepositoryPort.findByIdempotencyKeyAndResource(idempotencyKey, resourcePath);
        if (existing.isPresent()) {
            IdempotencyKeyRepositoryPort.StoredResponse stored = existing.get();
            httpResponse.setHeader(IDEMPOTENCY_REPLAY_HEADER, "true");
            httpResponse.setStatus(stored.responseStatus());
            if (stored.responseBody() != null) {
                httpResponse.getWriter().write(stored.responseBody());
                httpResponse.setContentType("application/json");
            }
            return;
        }

        boolean reserved = idempotencyKeyRepositoryPort.tryReserve(idempotencyKey, resourcePath, requestHash);
        if (!reserved) {
            httpResponse.sendError(HttpStatus.CONFLICT.value(), "Idempotency key already in use");
            return;
        }

        IdempotencyResponseWrapper wrapper = new IdempotencyResponseWrapper(httpResponse);
        chain.doFilter(httpRequest, wrapper);

        if (wrapper.getStatus() >= 200 && wrapper.getStatus() < 500) {
            idempotencyKeyRepositoryPort.complete(
                    idempotencyKey, resourcePath, requestHash,
                    wrapper.getStatus(), wrapper.getBodyAsString()
            );
        }
    }

    private boolean isIdempotentPath(String path) {
        return path.startsWith("/api/payments") || path.startsWith("/api/orders");
    }

    private String hashRequest(String body) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(body.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to hash request", e);
        }
    }
}
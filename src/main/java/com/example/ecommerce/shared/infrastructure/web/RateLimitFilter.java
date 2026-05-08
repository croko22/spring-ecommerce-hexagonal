package com.example.ecommerce.shared.infrastructure.web;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple in-memory rate limiter filter.
 * For production, consider Bucket4j or gateway-level rate limiting.
 */
@Component
public class RateLimitFilter implements Filter {

    // Simple in-memory rate limiting by IP and endpoint
    private final Map<String, RateLimitEntry> rateLimitMap = new ConcurrentHashMap<>();
    
    // Rate limits per endpoint pattern (requests per minute)
    private static final int AUTH_LIMIT = 5;
    private static final int ORDER_LIMIT = 20;
    private static final int PAYMENT_LIMIT = 10;
    private static final int DEFAULT_LIMIT = 60;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String clientIp = getClientIP(httpRequest);
        String endpoint = httpRequest.getRequestURI();
        
        int limit = getLimitForEndpoint(endpoint);
        
        String key = clientIp + ":" + endpoint;
        RateLimitEntry entry = rateLimitMap.computeIfAbsent(key, k -> new RateLimitEntry(limit));
        
        synchronized (entry) {
            if (entry.isExpired()) {
                entry.reset(limit);
            }
            
            if (entry.increment() > limit) {
                httpResponse.setStatus(429); // Too Many Requests
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write("{\"error\":\"Rate limit exceeded. Try again later.\"}");
                return;
            }
        }
        
        chain.doFilter(request, response);
    }

    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private int getLimitForEndpoint(String endpoint) {
        if (endpoint.contains("/auth/") || endpoint.contains("/api/auth")) {
            return AUTH_LIMIT;
        } else if (endpoint.contains("/api/orders")) {
            return ORDER_LIMIT;
        } else if (endpoint.contains("/api/payments")) {
            return PAYMENT_LIMIT;
        }
        return DEFAULT_LIMIT;
    }

    private static class RateLimitEntry {
        private final AtomicInteger counter = new AtomicInteger(0);
        private final long createdAt = System.currentTimeMillis();
        private final int limit;
        
        RateLimitEntry(int limit) {
            this.limit = limit;
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - createdAt > 60_000; // 1 minute
        }
        
        void reset(int newLimit) {
            counter.set(0);
        }
        
        int increment() {
            return counter.incrementAndGet();
        }
    }
}
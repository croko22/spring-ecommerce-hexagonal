package com.example.ecommerce.payment.infrastructure.adapter.out.security;

import com.example.ecommerce.payment.application.exception.PaymentAccessDeniedException;
import com.example.ecommerce.payment.application.port.out.PrincipalAccessPort;
import com.example.ecommerce.user.infrastructure.adapter.in.web.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityPrincipalAccessAdapter implements PrincipalAccessPort {

    @Override
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new PaymentAccessDeniedException("Authenticated principal is required for payment flow");
        }
        return principal.getUserId();
    }
}

package com.example.ecommerce.order.infrastructure.adapter.out.persistence;

import com.example.ecommerce.order.application.port.out.DiscountCodePort;
import com.example.ecommerce.order.domain.model.DiscountCode;
import com.example.ecommerce.order.domain.model.DiscountType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class DiscountCodePersistenceAdapter implements DiscountCodePort {

    // In-memory fallback for when DB is unavailable
    private static final List<DiscountCode> DEFAULT_CODES = List.of(
            new DiscountCode("DESCUENTO10", DiscountType.PERCENTAGE, 10),
            new DiscountCode("ENVIOGRATIS", DiscountType.FREE_SHIPPING, 100),
            new DiscountCode("AHORRA20", DiscountType.PERCENTAGE, 20)
    );

    @Override
    public Optional<DiscountCode> findByCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return DEFAULT_CODES.stream()
                .filter(dc -> dc.getCode().equalsIgnoreCase(code.trim()))
                .filter(DiscountCode::isActive)
                .findFirst();
    }

    @Override
    public boolean isValid(String code) {
        return findByCode(code).isPresent();
    }
}
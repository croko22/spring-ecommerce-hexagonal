package com.example.ecommerce.order.application.port.out;

import com.example.ecommerce.order.domain.model.DiscountCode;

import java.util.Optional;

public interface DiscountCodePort {

    Optional<DiscountCode> findByCode(String code);

    boolean isValid(String code);
}
package com.example.ecommerce.payment.application.service;

import com.example.ecommerce.payment.application.port.out.OrderSnapshotPort;
import com.example.ecommerce.payment.domain.model.CurrencyCode;
import com.example.ecommerce.payment.domain.model.Money;

import java.math.BigDecimal;

public class OrderToPaymentMapper {

    public Money toMoney(OrderSnapshotPort.OrderSnapshot orderSnapshot) {
        BigDecimal normalized = BigDecimal.valueOf(orderSnapshot.totalAmount());
        return new Money(normalized, new CurrencyCode(resolveCurrency(orderSnapshot.currency())));
    }

    private String resolveCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            return "USD";
        }
        return currency;
    }
}

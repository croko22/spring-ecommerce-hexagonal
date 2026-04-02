package com.example.ecommerce.payment.application.service;

import com.example.ecommerce.payment.application.port.out.OrderSnapshotPort;
import com.example.ecommerce.payment.domain.model.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderToPaymentMapperTest {

    @Test
    void shouldConvertLegacyDoubleAmountToMoneyUsingHalfUpScale() {
        OrderToPaymentMapper mapper = new OrderToPaymentMapper();

        OrderSnapshotPort.OrderSnapshot snapshot = new OrderSnapshotPort.OrderSnapshot(
                1L,
                2L,
                "PENDING",
                10.235,
                "usd"
        );

        Money money = mapper.toMoney(snapshot);

        assertEquals(new BigDecimal("10.24"), money.getAmount());
        assertEquals("USD", money.getCurrency().getValue());
    }

    @Test
    void shouldDefaultCurrencyToUsdWhenMissingInOrderSnapshot() {
        OrderToPaymentMapper mapper = new OrderToPaymentMapper();

        OrderSnapshotPort.OrderSnapshot snapshot = new OrderSnapshotPort.OrderSnapshot(
                1L,
                2L,
                "PENDING",
                10.0,
                null
        );

        Money money = mapper.toMoney(snapshot);

        assertEquals("USD", money.getCurrency().getValue());
    }
}

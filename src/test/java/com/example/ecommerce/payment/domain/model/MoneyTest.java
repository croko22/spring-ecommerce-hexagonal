package com.example.ecommerce.payment.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoneyTest {

    @Test
    void shouldNormalizeScaleWithHalfUpRounding() {
        Money money = new Money(new BigDecimal("10.125"), new CurrencyCode("usd"));

        assertEquals(new BigDecimal("10.13"), money.getAmount());
        assertEquals("USD", money.getCurrency().getValue());
    }

    @Test
    void shouldThrowExceptionWhenAmountIsNegative() {
        assertThrows(IllegalArgumentException.class, () ->
                new Money(new BigDecimal("-0.01"), new CurrencyCode("USD"))
        );
    }
}

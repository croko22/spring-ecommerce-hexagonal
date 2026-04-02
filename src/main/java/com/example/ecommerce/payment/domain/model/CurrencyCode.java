package com.example.ecommerce.payment.domain.model;

import java.util.Locale;

public class CurrencyCode {

    private final String value;

    public CurrencyCode(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Currency code cannot be blank");
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (!normalized.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("Currency code must be a 3-letter ISO-4217 code");
        }

        this.value = normalized;
    }

    public String getValue() {
        return value;
    }

    public int minorUnits() {
        return 2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CurrencyCode that = (CurrencyCode) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}

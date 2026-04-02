package com.example.ecommerce.payment.domain.model;

public class ProviderReference {

    private final String value;

    public ProviderReference(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Provider reference cannot be blank");
        }
        this.value = value.trim();
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProviderReference that = (ProviderReference) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}

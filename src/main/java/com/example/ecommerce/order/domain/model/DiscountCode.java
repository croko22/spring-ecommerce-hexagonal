package com.example.ecommerce.order.domain.model;

public class DiscountCode {

    private final String code;
    private final DiscountType type;
    private final double value;
    private final boolean active;

    public DiscountCode(String code, DiscountType type, double value) {
        this(code, type, value, true);
    }

    public DiscountCode(String code, DiscountType type, double value, boolean active) {
        this.code = code;
        this.type = type;
        this.value = value;
        this.active = active;
    }

    public String getCode() {
        return code;
    }

    public DiscountType getType() {
        return type;
    }

    public double getValue() {
        return value;
    }

    public boolean isActive() {
        return active;
    }

    public double calculateDiscount(double orderSubtotal, double shippingCost) {
        if (!active) return 0;

        return switch (type) {
            case PERCENTAGE -> orderSubtotal * (value / 100);
            case FREE_SHIPPING -> shippingCost;
        };
    }
}
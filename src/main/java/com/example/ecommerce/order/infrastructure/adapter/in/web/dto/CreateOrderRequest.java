package com.example.ecommerce.order.infrastructure.adapter.in.web.dto;

import com.example.ecommerce.order.domain.model.ShippingAddress;
import java.util.List;

public class CreateOrderRequest {

    private List<OrderItemRequest> items;
    private ShippingAddressRequest shipping;
    private String paymentMethod;
    private String discountCode;
    private CreditCardRequest creditCard;

    // Getters and Setters
    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }

    public ShippingAddressRequest getShipping() {
        return shipping;
    }

    public void setShipping(ShippingAddressRequest shipping) {
        this.shipping = shipping;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getDiscountCode() {
        return discountCode;
    }

    public void setDiscountCode(String discountCode) {
        this.discountCode = discountCode;
    }

    public CreditCardRequest getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(CreditCardRequest creditCard) {
        this.creditCard = creditCard;
    }

    // Nested DTOs
    public static class OrderItemRequest {
        private String productId;
        private String name;
        private int quantity;
        private double price;

        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
    }

    public static class ShippingAddressRequest {
        private String fullName;
        private String documentType;
        private String documentNumber;
        private String address;
        private String phone;
        private String region;

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getDocumentType() { return documentType; }
        public void setDocumentType(String documentType) { this.documentType = documentType; }
        public String getDocumentNumber() { return documentNumber; }
        public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
    }

    public static class CreditCardRequest {
        private String cardNumber;
        private String expiryDate;
        private String cvv;
        private String cardholderName;

        public String getCardNumber() { return cardNumber; }
        public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
        public String getExpiryDate() { return expiryDate; }
        public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
        public String getCvv() { return cvv; }
        public void setCvv(String cvv) { this.cvv = cvv; }
        public String getCardholderName() { return cardholderName; }
        public void setCardholderName(String cardholderName) { this.cardholderName = cardholderName; }
    }
}

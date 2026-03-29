package com.example.ecommerce.order.infrastructure.adapter.out.persistence;

import com.example.ecommerce.order.domain.model.ShippingAddress;

import jakarta.persistence.Embeddable;

@Embeddable
public class ShippingAddressEmbeddable {

    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;

    public ShippingAddressEmbeddable() {
    }

    public ShippingAddressEmbeddable(String street, String city, String state, String zipCode, String country) {
        this.street = street;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.country = country;
    }

    public static ShippingAddressEmbeddable fromDomain(ShippingAddress address) {
        return new ShippingAddressEmbeddable(
                address.getStreet(),
                address.getCity(),
                address.getState(),
                address.getZipCode(),
                address.getCountry()
        );
    }

    public ShippingAddress toDomain() {
        return new ShippingAddress(street, city, state, zipCode, country);
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}

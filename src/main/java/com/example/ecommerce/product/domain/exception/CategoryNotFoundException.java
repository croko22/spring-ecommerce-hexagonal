package com.example.ecommerce.product.domain.exception;

public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(Long id) {
        super("Category with ID " + id + " not found");
    }
}
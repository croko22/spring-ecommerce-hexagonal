package com.example.ecommerce.product.domain.model;

import com.example.ecommerce.product.domain.exception.InvalidCategoryException;

public class Category {

    private Long id;
    private String name;
    private String description;

    public Category(Long id, String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidCategoryException("Category name cannot be blank");
        }
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidCategoryException("Category name cannot be blank");
        }
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
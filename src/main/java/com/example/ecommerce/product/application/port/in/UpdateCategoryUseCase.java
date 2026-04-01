package com.example.ecommerce.product.application.port.in;

import com.example.ecommerce.product.domain.model.Category;

public interface UpdateCategoryUseCase {

    Category updateCategory(Long id, Category category);
}
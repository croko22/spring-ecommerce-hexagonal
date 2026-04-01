package com.example.ecommerce.product.application.port.in;

import com.example.ecommerce.product.domain.model.Category;

public interface CreateCategoryUseCase {

    Category createCategory(Category category);
}
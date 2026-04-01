package com.example.ecommerce.product.application.port.in;

import com.example.ecommerce.product.domain.model.Category;
import java.util.List;

public interface GetCategoryUseCase {

    Category getCategoryById(Long id);
    
    List<Category> getAllCategories();
}
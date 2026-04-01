package com.example.ecommerce.product.application.port.out;

import com.example.ecommerce.product.domain.model.Category;
import java.util.List;
import java.util.Optional;

public interface CategoryRepositoryPort {

    Category save(Category category);
    
    Optional<Category> findById(Long id);
    
    List<Category> findAll();
    
    void deleteById(Long id);
}
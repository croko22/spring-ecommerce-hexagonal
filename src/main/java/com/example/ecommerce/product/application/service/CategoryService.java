package com.example.ecommerce.product.application.service;

import com.example.ecommerce.product.application.port.in.CreateCategoryUseCase;
import com.example.ecommerce.product.application.port.in.DeleteCategoryUseCase;
import com.example.ecommerce.product.application.port.in.GetCategoryUseCase;
import com.example.ecommerce.product.application.port.in.UpdateCategoryUseCase;
import com.example.ecommerce.product.application.port.out.CategoryRepositoryPort;
import com.example.ecommerce.product.domain.exception.CategoryNotFoundException;
import com.example.ecommerce.product.domain.model.Category;

import java.util.List;

public class CategoryService implements CreateCategoryUseCase, GetCategoryUseCase, UpdateCategoryUseCase, DeleteCategoryUseCase {

    private final CategoryRepositoryPort categoryRepositoryPort;

    public CategoryService(CategoryRepositoryPort categoryRepositoryPort) {
        this.categoryRepositoryPort = categoryRepositoryPort;
    }

    @Override
    public Category createCategory(Category category) {
        return categoryRepositoryPort.save(category);
    }

    @Override
    public Category getCategoryById(Long id) {
        return categoryRepositoryPort.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepositoryPort.findAll();
    }

    @Override
    public Category updateCategory(Long id, Category categoryToUpdate) {
        Category existingCategory = getCategoryById(id);
        
        existingCategory.setName(categoryToUpdate.getName());
        existingCategory.setDescription(categoryToUpdate.getDescription());
        
        return categoryRepositoryPort.save(existingCategory);
    }

    @Override
    public void deleteCategory(Long id) {
        getCategoryById(id); // Check if exists
        categoryRepositoryPort.deleteById(id);
    }
}
package com.example.ecommerce.product.infrastructure.adapter.out.persistence;

import com.example.ecommerce.product.application.port.out.CategoryRepositoryPort;
import com.example.ecommerce.product.domain.model.Category;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CategoryPersistenceAdapter implements CategoryRepositoryPort {

    private final CategoryJpaRepository categoryJpaRepository;

    public CategoryPersistenceAdapter(CategoryJpaRepository categoryJpaRepository) {
        this.categoryJpaRepository = categoryJpaRepository;
    }

    @Override
    public Category save(Category category) {
        CategoryEntity entity = new CategoryEntity();
        if (category.getId() != null) {
            entity.setId(category.getId());
        }
        entity.setName(category.getName());
        entity.setDescription(category.getDescription());

        CategoryEntity savedEntity = categoryJpaRepository.save(entity);
        return mapToDomainModel(savedEntity);
    }

    @Override
    public Optional<Category> findById(Long id) {
        return categoryJpaRepository.findById(id).map(this::mapToDomainModel);
    }

    @Override
    public List<Category> findAll() {
        return categoryJpaRepository.findAll().stream()
                .map(this::mapToDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        categoryJpaRepository.deleteById(id);
    }

    private Category mapToDomainModel(CategoryEntity entity) {
        return new Category(
                entity.getId(),
                entity.getName(),
                entity.getDescription()
        );
    }
}
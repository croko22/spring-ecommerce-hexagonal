package com.example.ecommerce.product.infrastructure.adapter.out.persistence;

import com.example.ecommerce.product.application.port.out.ProductRepositoryPort;
import com.example.ecommerce.product.domain.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ProductPersistenceAdapter implements ProductRepositoryPort {

    private final ProductJpaRepository productJpaRepository;

    public ProductPersistenceAdapter(ProductJpaRepository productJpaRepository) {
        this.productJpaRepository = productJpaRepository;
    }

    @Override
    public Product save(Product product) {
        // Map Domain Model to Entity
        ProductEntity entity = new ProductEntity();
        if (product.getId() != null) {
            entity.setId(product.getId());
        }
        entity.setName(product.getName());
        entity.setDescription(product.getDescription());
        entity.setPrice(product.getPrice());
        entity.setStock(product.getStock());
        entity.setImageUrl(product.getImageUrl());
        entity.setSku(product.getSku());
        entity.setCategoryId(product.getCategoryId());
        
        // Save using Spring Data JPA
        ProductEntity savedEntity = productJpaRepository.save(entity);
        
        // Map back to Domain Model
        return mapToDomainModel(savedEntity);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return productJpaRepository.findById(id).map(this::mapToDomainModel);
    }

    @Override
    public List<Product> findAll() {
        return productJpaRepository.findAll().stream()
                .map(this::mapToDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        productJpaRepository.deleteById(id);
    }

    @Override
    public Page<Product> findByFilters(Long categoryId, Double minPrice, Double maxPrice,
                                       Boolean inStock, String search, Pageable pageable) {
        return productJpaRepository.findByFilters(categoryId, minPrice, maxPrice, inStock, search, pageable)
                .map(this::mapToDomainModel);
    }

    @Override
    public List<Product> findByCategoryId(Long categoryId) {
        return productJpaRepository.findByCategoryId(categoryId).stream()
                .map(this::mapToDomainModel)
                .collect(Collectors.toList());
    }
    
    private Product mapToDomainModel(ProductEntity entity) {
        return new Product(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getStock(),
                entity.getImageUrl(),
                entity.getSku(),
                entity.getCategoryId()
        );
    }
}
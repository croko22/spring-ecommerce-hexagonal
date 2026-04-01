package com.example.ecommerce.product.application.port.out;

import com.example.ecommerce.product.domain.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductRepositoryPort {

    Product save(Product product);
    
    Optional<Product> findById(Long id);
    
    List<Product> findAll();
    
    void deleteById(Long id);

    Page<Product> findByFilters(Long categoryId, Double minPrice, Double maxPrice,
                                  Boolean inStock, String search, Pageable pageable);

    List<Product> findByCategoryId(Long categoryId);
}
package com.example.ecommerce.product.application.port.in;

import com.example.ecommerce.product.domain.model.Product;

public interface UpdateProductUseCase {
    Product updateProduct(Long id, Product product);
}
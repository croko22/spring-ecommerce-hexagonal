package com.example.ecommerce.product.application.port.in;

import com.example.ecommerce.product.domain.model.Product;
import java.util.List;

public interface GetProductUseCase {
    Product getProductById(Long id);
    List<Product> getAllProducts();
}
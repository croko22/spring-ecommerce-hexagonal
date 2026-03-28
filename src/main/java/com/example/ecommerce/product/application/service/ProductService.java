package com.example.ecommerce.product.application.service;

import com.example.ecommerce.product.application.port.in.CreateProductUseCase;
import com.example.ecommerce.product.application.port.in.DeleteProductUseCase;
import com.example.ecommerce.product.application.port.in.GetProductUseCase;
import com.example.ecommerce.product.application.port.in.UpdateProductUseCase;
import com.example.ecommerce.product.application.port.out.ProductRepositoryPort;
import com.example.ecommerce.product.domain.exception.ProductNotFoundException;
import com.example.ecommerce.product.domain.model.Product;

import java.util.List;

public class ProductService implements CreateProductUseCase, GetProductUseCase, UpdateProductUseCase, DeleteProductUseCase {

    private final ProductRepositoryPort productRepositoryPort;

    public ProductService(ProductRepositoryPort productRepositoryPort) {
        this.productRepositoryPort = productRepositoryPort;
    }

    @Override
    public Product createProduct(Product product) {
        return productRepositoryPort.save(product);
    }

    @Override
    public Product getProductById(Long id) {
        return productRepositoryPort.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepositoryPort.findAll();
    }

    @Override
    public Product updateProduct(Long id, Product productToUpdate) {
        Product existingProduct = getProductById(id);
        
        existingProduct.setName(productToUpdate.getName());
        existingProduct.setDescription(productToUpdate.getDescription());
        existingProduct.setPrice(productToUpdate.getPrice());
        
        return productRepositoryPort.save(existingProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        getProductById(id); // Check if exists
        productRepositoryPort.deleteById(id);
    }
}
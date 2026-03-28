package com.example.ecommerce.product.infrastructure.config;

import com.example.ecommerce.product.application.port.in.CreateProductUseCase;
import com.example.ecommerce.product.application.port.in.DeleteProductUseCase;
import com.example.ecommerce.product.application.port.in.GetProductUseCase;
import com.example.ecommerce.product.application.port.in.UpdateProductUseCase;
import com.example.ecommerce.product.application.port.out.ProductRepositoryPort;
import com.example.ecommerce.product.application.service.ProductService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProductConfig {

    @Bean
    public ProductService productService(ProductRepositoryPort productRepositoryPort) {
        return new ProductService(productRepositoryPort);
    }

    @Bean
    public CreateProductUseCase createProductUseCase(ProductService productService) {
        return productService;
    }

    @Bean
    public GetProductUseCase getProductUseCase(ProductService productService) {
        return productService;
    }

    @Bean
    public UpdateProductUseCase updateProductUseCase(ProductService productService) {
        return productService;
    }

    @Bean
    public DeleteProductUseCase deleteProductUseCase(ProductService productService) {
        return productService;
    }
}
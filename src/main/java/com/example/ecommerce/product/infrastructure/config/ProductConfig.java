package com.example.ecommerce.product.infrastructure.config;

import com.example.ecommerce.product.application.port.in.CreateCategoryUseCase;
import com.example.ecommerce.product.application.port.in.CreateProductUseCase;
import com.example.ecommerce.product.application.port.in.DeleteCategoryUseCase;
import com.example.ecommerce.product.application.port.in.DeleteProductUseCase;
import com.example.ecommerce.product.application.port.in.GetCategoryUseCase;
import com.example.ecommerce.product.application.port.in.GetProductUseCase;
import com.example.ecommerce.product.application.port.in.UpdateCategoryUseCase;
import com.example.ecommerce.product.application.port.in.UpdateProductUseCase;
import com.example.ecommerce.product.application.port.out.CategoryRepositoryPort;
import com.example.ecommerce.product.application.port.out.ProductRepositoryPort;
import com.example.ecommerce.product.application.service.CategoryService;
import com.example.ecommerce.product.application.service.ProductService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProductConfig {

    @Bean
    public ProductService productService(ProductRepositoryPort productRepositoryPort,
                                         CategoryRepositoryPort categoryRepositoryPort) {
        return new ProductService(productRepositoryPort, categoryRepositoryPort);
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

    @Bean
    public CategoryService categoryService(CategoryRepositoryPort categoryRepositoryPort) {
        return new CategoryService(categoryRepositoryPort);
    }

    @Bean
    public CreateCategoryUseCase createCategoryUseCase(CategoryService categoryService) {
        return categoryService;
    }

    @Bean
    public GetCategoryUseCase getCategoryUseCase(CategoryService categoryService) {
        return categoryService;
    }

    @Bean
    public UpdateCategoryUseCase updateCategoryUseCase(CategoryService categoryService) {
        return categoryService;
    }

    @Bean
    public DeleteCategoryUseCase deleteCategoryUseCase(CategoryService categoryService) {
        return categoryService;
    }
}

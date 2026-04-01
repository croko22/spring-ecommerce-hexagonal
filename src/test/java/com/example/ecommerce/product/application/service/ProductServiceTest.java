package com.example.ecommerce.product.application.service;

import com.example.ecommerce.product.application.port.out.ProductRepositoryPort;
import com.example.ecommerce.product.application.port.out.CategoryRepositoryPort;
import com.example.ecommerce.product.domain.exception.ProductNotFoundException;
import com.example.ecommerce.product.domain.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepositoryPort productRepositoryPort;

    @Mock
    private CategoryRepositoryPort categoryRepositoryPort;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepositoryPort, categoryRepositoryPort);
    }

    @Test
    void shouldCreateProductSuccessfully() {
        Product productToCreate = new Product(null, "Test Product", "A great product", 99.99);
        Product savedProduct = new Product(1L, "Test Product", "A great product", 99.99);

        when(productRepositoryPort.save(any(Product.class))).thenReturn(savedProduct);

        Product createdProduct = productService.createProduct(productToCreate);

        assertNotNull(createdProduct);
        assertEquals(1L, createdProduct.getId());
        assertEquals("Test Product", createdProduct.getName());

        verify(productRepositoryPort).save(any(Product.class));
    }

    @Test
    void shouldReturnProductWhenIdExists() {
        Product existingProduct = new Product(1L, "Test Product", "A great product", 99.99);
        when(productRepositoryPort.findById(1L)).thenReturn(Optional.of(existingProduct));

        Product result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Product", result.getName());
    }

    @Test
    void shouldThrowExceptionWhenIdDoesNotExist() {
        when(productRepositoryPort.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(99L));
    }

    @Test
    void shouldReturnAllProducts() {
        List<Product> products = List.of(
                new Product(1L, "Product 1", "Description 1", 10.0),
                new Product(2L, "Product 2", "Description 2", 20.0)
        );
        when(productRepositoryPort.findAll()).thenReturn(products);

        List<Product> result = productService.getAllProducts();

        assertEquals(2, result.size());
        assertEquals("Product 1", result.get(0).getName());
    }

    @Test
    void shouldUpdateProductSuccessfully() {
        Product existingProduct = new Product(1L, "Old Name", "Old Desc", 10.0);
        Product updateRequest = new Product(null, "New Name", "New Desc", 20.0);
        Product updatedProduct = new Product(1L, "New Name", "New Desc", 20.0);

        when(productRepositoryPort.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepositoryPort.save(any(Product.class))).thenReturn(updatedProduct);

        Product result = productService.updateProduct(1L, updateRequest);

        assertEquals("New Name", result.getName());
        assertEquals(20.0, result.getPrice());
        verify(productRepositoryPort).save(any(Product.class));
    }

    @Test
    void shouldDeleteProductSuccessfully() {
        Product existingProduct = new Product(1L, "Test Product", "A great product", 99.99);
        when(productRepositoryPort.findById(1L)).thenReturn(Optional.of(existingProduct));

        productService.deleteProduct(1L);

        verify(productRepositoryPort).findById(1L);
        verify(productRepositoryPort).deleteById(1L);
    }
}

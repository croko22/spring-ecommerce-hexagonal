package com.example.ecommerce.product.infrastructure.adapter.out.persistence;

import com.example.ecommerce.product.domain.model.Product;
import com.example.ecommerce.shared.infrastructure.PostgresContainerIntegrationTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Tag("integration")
class ProductPersistenceAdapterTest extends PostgresContainerIntegrationTest {

    @Autowired
    private ProductPersistenceAdapter adapter;

    @Autowired
    private ProductJpaRepository repository;

    @Test
    void shouldSaveAndReturnProduct() {
        Product product = new Product(null, "Mouse", "Wireless Mouse", 25.50);

        Product savedProduct = adapter.save(product);

        assertNotNull(savedProduct.getId());
        assertEquals("Mouse", savedProduct.getName());
        
        // Verify it was actually saved in DB
        assertTrue(repository.findById(savedProduct.getId()).isPresent());
    }

    @Test
    void shouldFindProductById() {
        // Arrange (Save entity directly using repository)
        ProductEntity entity = new ProductEntity();
        entity.setName("Keyboard");
        entity.setDescription("Mechanical");
        entity.setPrice(100.0);
        ProductEntity savedEntity = repository.save(entity);

        // Act
        Optional<Product> foundProduct = adapter.findById(savedEntity.getId());

        // Assert
        assertTrue(foundProduct.isPresent());
        assertEquals("Keyboard", foundProduct.get().getName());
    }

    @Test
    void shouldReturnEmptyWhenProductNotFound() {
        Optional<Product> foundProduct = adapter.findById(999L);
        assertFalse(foundProduct.isPresent());
    }

    @Test
    void shouldFindAllProducts() {
        ProductEntity entity1 = new ProductEntity();
        entity1.setName("Item 1");
        entity1.setPrice(10.0);
        
        ProductEntity entity2 = new ProductEntity();
        entity2.setName("Item 2");
        entity2.setPrice(20.0);
        
        repository.save(entity1);
        repository.save(entity2);

        List<Product> products = adapter.findAll();

        assertTrue(products.size() >= 2);
    }

    @Test
    void shouldDeleteProduct() {
        ProductEntity entity = new ProductEntity();
        entity.setName("To Be Deleted");
        entity.setPrice(10.0);
        ProductEntity savedEntity = repository.save(entity);

        adapter.deleteById(savedEntity.getId());

        assertFalse(repository.findById(savedEntity.getId()).isPresent());
    }
}

package com.example.ecommerce.product.infrastructure.adapter.in.web;

import com.example.ecommerce.product.application.port.in.CreateProductUseCase;
import com.example.ecommerce.product.application.port.in.DeleteProductUseCase;
import com.example.ecommerce.product.application.port.in.GetProductUseCase;
import com.example.ecommerce.product.application.port.in.UpdateProductUseCase;
import com.example.ecommerce.product.application.service.ProductService;
import com.example.ecommerce.product.domain.exception.CategoryNotFoundException;
import com.example.ecommerce.product.domain.exception.ProductNotFoundException;
import com.example.ecommerce.product.domain.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CreateProductUseCase createProductUseCase;

    @Mock
    private GetProductUseCase getProductUseCase;

    @Mock
    private UpdateProductUseCase updateProductUseCase;

    @Mock
    private DeleteProductUseCase deleteProductUseCase;

    @Mock
    private ProductService productService;

    @BeforeEach
    void setUp() {
        ProductController productController = new ProductController(
                createProductUseCase, getProductUseCase, updateProductUseCase, deleteProductUseCase, productService);
        
        mockMvc = MockMvcBuilders.standaloneSetup(productController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldCreateProductAndReturn201() throws Exception {
        String requestBody = "{\"name\":\"Laptop\",\"description\":\"Gaming Laptop\",\"price\":1500.0,\"stock\":15,\"imageUrl\":\"https://img/laptop.png\",\"sku\":\"LAP-001\",\"categoryId\":1}";
        Product product = new Product(1L, "Laptop", "Gaming Laptop", 1500.0, 15, "https://img/laptop.png", "LAP-001", 1L);

        when(createProductUseCase.createProduct(any(Product.class))).thenReturn(product);

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.stock").value(15))
                .andExpect(jsonPath("$.imageUrl").value("https://img/laptop.png"))
                .andExpect(jsonPath("$.sku").value("LAP-001"))
                .andExpect(jsonPath("$.categoryId").value(1));
    }

    @Test
    void shouldGetProductAndReturn200() throws Exception {
        Product product = new Product(1L, "Laptop", "Gaming Laptop", 1500.0, 12, "https://img/laptop.png", "LAP-001", 3L);
        when(getProductUseCase.getProductById(1L)).thenReturn(product);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.stock").value(12))
                .andExpect(jsonPath("$.imageUrl").value("https://img/laptop.png"))
                .andExpect(jsonPath("$.sku").value("LAP-001"))
                .andExpect(jsonPath("$.categoryId").value(3));
    }

    @Test
    void shouldReturn404WhenProductNotFound() throws Exception {
        when(getProductUseCase.getProductById(99L)).thenThrow(new ProductNotFoundException(99L));

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Product with ID 99 not found"));
    }

    @Test
    void shouldReturnAllProductsAndReturn200() throws Exception {
        List<Product> products = List.of(
                new Product(1L, "Product 1", "Desc 1", 10.0),
                new Product(2L, "Product 2", "Desc 2", 20.0)
        );
        Page<Product> productPage = new PageImpl<>(products, PageRequest.of(0, 20), 2);
        when(productService.searchProducts(any(), any(), any(), any(), any(), any(Pageable.class))).thenReturn(productPage);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Product 1"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    void shouldApplyCombinedFiltersAndReturn200() throws Exception {
        List<Product> products = List.of(
                new Product(1L, "Gaming Laptop", "Powerful laptop", 1500.0, 5, "img", "SKU-1", 1L)
        );
        Page<Product> page = new PageImpl<>(products, PageRequest.of(0, 20), 1);

        when(productService.searchProducts(eq(1L), eq(1000.0), eq(2000.0), eq(true), eq("laptop"), eq(PageRequest.of(0, 20))))
                .thenReturn(page);

        mockMvc.perform(get("/api/products")
                        .param("categoryId", "1")
                        .param("minPrice", "1000")
                        .param("maxPrice", "2000")
                        .param("inStock", "true")
                        .param("search", "laptop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Gaming Laptop"));

        verify(productService).searchProducts(1L, 1000.0, 2000.0, true, "laptop", PageRequest.of(0, 20));
    }

    @Test
    void shouldReturnCustomPaginationMetadata() throws Exception {
        List<Product> products = List.of(
                new Product(11L, "Product 11", "Desc", 11.0),
                new Product(12L, "Product 12", "Desc", 12.0)
        );
        Page<Product> page = new PageImpl<>(products, PageRequest.of(1, 10), 25);

        when(productService.searchProducts(any(), any(), any(), any(), any(), eq(PageRequest.of(1, 10)))).thenReturn(page);

        mockMvc.perform(get("/api/products")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(25))
                .andExpect(jsonPath("$.totalPages").value(3));

        verify(productService).searchProducts(null, null, null, null, null, PageRequest.of(1, 10));
    }

    @Test
    void shouldUpdateProductAndReturn200() throws Exception {
        String requestBody = "{\"name\":\"Updated Laptop\",\"description\":\"Gaming Laptop\",\"price\":1600.0}";
        Product updatedProduct = new Product(1L, "Updated Laptop", "Gaming Laptop", 1600.0);

        when(updateProductUseCase.updateProduct(eq(1L), any(Product.class))).thenReturn(updatedProduct);

        mockMvc.perform(put("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Laptop"))
                .andExpect(jsonPath("$.price").value(1600.0));
    }

    @Test
    void shouldReturn404WhenAssigningNonExistentCategoryOnUpdate() throws Exception {
        String requestBody = "{\"name\":\"Laptop\",\"description\":\"Gaming Laptop\",\"price\":1500.0,\"stock\":10,\"categoryId\":999}";

        when(updateProductUseCase.updateProduct(eq(1L), any(Product.class)))
                .thenThrow(new CategoryNotFoundException(999L));

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Category with ID 999 not found"));
    }

    @Test
    void shouldReturn400WhenCreatingProductWithNegativePrice() throws Exception {
        String requestBody = "{\"name\":\"Laptop\",\"description\":\"Gaming Laptop\",\"price\":-1.0,\"stock\":10}";

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Price cannot be negative"));
    }

    @Test
    void shouldDeleteProductAndReturn204() throws Exception {
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }
}

package com.example.ecommerce.product.infrastructure.adapter.in.web;

import com.example.ecommerce.product.application.port.in.CreateProductUseCase;
import com.example.ecommerce.product.application.port.in.DeleteProductUseCase;
import com.example.ecommerce.product.application.port.in.GetProductUseCase;
import com.example.ecommerce.product.application.port.in.UpdateProductUseCase;
import com.example.ecommerce.product.application.service.ProductService;
import com.example.ecommerce.product.domain.model.Product;
import com.example.ecommerce.product.infrastructure.adapter.in.web.dto.ProductRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Product management endpoints")
public class ProductController {

    private final CreateProductUseCase createProductUseCase;
    private final GetProductUseCase getProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final DeleteProductUseCase deleteProductUseCase;
    private final ProductService productService;

    public ProductController(CreateProductUseCase createProductUseCase,
                             GetProductUseCase getProductUseCase,
                             UpdateProductUseCase updateProductUseCase,
                             DeleteProductUseCase deleteProductUseCase,
                             ProductService productService) {
        this.createProductUseCase = createProductUseCase;
        this.getProductUseCase = getProductUseCase;
        this.updateProductUseCase = updateProductUseCase;
        this.deleteProductUseCase = deleteProductUseCase;
        this.productService = productService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a product", description = "Creates a new product with name, description and price")
    public Product createProduct(@RequestBody ProductRequest productRequest) {
        Product product = new Product(null, productRequest.getName(), productRequest.getDescription(),
                productRequest.getPrice(), productRequest.getStock(), productRequest.getImageUrl(),
                productRequest.getSku(), productRequest.getCategoryId());
        return createProductUseCase.createProduct(product);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a product by ID", description = "Returns a single product")
    public Product getProduct(@PathVariable Long id) {
        return getProductUseCase.getProductById(id);
    }

    @GetMapping
    @Operation(summary = "Get all products", description = "Returns a list of all products with optional filtering and pagination")
    public Page<Product> getAllProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productService.searchProducts(categoryId, minPrice, maxPrice, inStock, search, pageable);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a product", description = "Updates an existing product")
    public Product updateProduct(@PathVariable Long id, @RequestBody ProductRequest productRequest) {
        Product product = new Product(id, productRequest.getName(), productRequest.getDescription(),
                productRequest.getPrice(), productRequest.getStock(), productRequest.getImageUrl(),
                productRequest.getSku(), productRequest.getCategoryId());
        return updateProductUseCase.updateProduct(id, product);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a product", description = "Deletes a product by ID")
    public void deleteProduct(@PathVariable Long id) {
        deleteProductUseCase.deleteProduct(id);
    }
}

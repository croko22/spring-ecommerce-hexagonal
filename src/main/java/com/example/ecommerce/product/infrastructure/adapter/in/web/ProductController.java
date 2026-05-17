package com.example.ecommerce.product.infrastructure.adapter.in.web;

import com.example.ecommerce.product.application.port.in.CreateProductUseCase;
import com.example.ecommerce.product.application.port.in.DeleteProductUseCase;
import com.example.ecommerce.product.application.port.in.GetProductUseCase;
import com.example.ecommerce.product.application.port.in.UpdateProductUseCase;
import com.example.ecommerce.product.application.service.ProductService;
import com.example.ecommerce.product.domain.model.Product;
import com.example.ecommerce.product.domain.model.StockHistory;
import com.example.ecommerce.product.infrastructure.adapter.in.web.dto.ProductRequest;
import com.example.ecommerce.product.infrastructure.adapter.in.web.dto.StockAdjustRequest;
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
        product.setLowStockThreshold(productRequest.getLowStockThreshold());
        return createProductUseCase.createProduct(product);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a product by ID", description = "Returns a single product")
    public Product getProduct(@PathVariable String id) {
        return getProductUseCase.getProductById(parseId(id));
    }

    private Long parseId(String id) {
        if (id != null && id.startsWith("p-")) {
            return Long.parseLong(id.substring(2));
        }
        return Long.parseLong(id);
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
    public Product updateProduct(@PathVariable String id, @RequestBody ProductRequest productRequest) {
        Long parsedId = parseId(id);
        Product product = new Product(parsedId, productRequest.getName(), productRequest.getDescription(),
                productRequest.getPrice(), productRequest.getStock(), productRequest.getImageUrl(),
                productRequest.getSku(), productRequest.getCategoryId());
        product.setLowStockThreshold(productRequest.getLowStockThreshold());
        return updateProductUseCase.updateProduct(parsedId, product);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a product", description = "Deletes a product by ID")
    public void deleteProduct(@PathVariable String id) {
        deleteProductUseCase.deleteProduct(parseId(id));
    }

    // ========== Stock management endpoints ==========

    @PostMapping("/{id}/stock/adjust")
    @Operation(summary = "Adjust product stock", description = "Adjusts stock by a delta amount (positive or negative)")
    public Product adjustStock(@PathVariable String id, @RequestBody StockAdjustRequest request) {
        Long parsedId = parseId(id);
        productService.adjustStock(parsedId, request.getQuantity(), request.getReason());
        return getProductUseCase.getProductById(parsedId);
    }

    @GetMapping("/{id}/stock/history")
    @Operation(summary = "Get stock history", description = "Returns stock change history for a product")
    public Page<StockHistory> getStockHistory(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productService.getStockHistory(parseId(id), pageable);
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get low stock products", description = "Returns all products flagged as low stock")
    public List<Product> getLowStockProducts() {
        return productService.getLowStockProducts();
    }
}

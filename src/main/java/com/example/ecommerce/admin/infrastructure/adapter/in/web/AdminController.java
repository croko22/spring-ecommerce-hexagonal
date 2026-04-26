package com.example.ecommerce.admin.infrastructure.adapter.in.web;

import com.example.ecommerce.admin.application.service.AdminService;
import com.example.ecommerce.admin.domain.model.DashboardMetrics;
import com.example.ecommerce.admin.infrastructure.adapter.in.web.dto.AdminOrderResponse;
import com.example.ecommerce.admin.infrastructure.adapter.in.web.dto.AdminUserResponse;
import com.example.ecommerce.admin.infrastructure.adapter.in.web.dto.DashboardResponse;
import com.example.ecommerce.admin.infrastructure.adapter.in.web.dto.UpdateRoleRequest;
import com.example.ecommerce.order.domain.model.Order;
import com.example.ecommerce.order.domain.model.OrderStatus;
import com.example.ecommerce.product.domain.model.Product;
import com.example.ecommerce.user.domain.model.User;
import com.example.ecommerce.user.domain.model.UserRole;
import com.example.ecommerce.product.infrastructure.adapter.in.web.dto.ProductRequest;
import com.example.ecommerce.product.infrastructure.adapter.in.web.dto.ProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Admin dashboard and management endpoints")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard metrics")
    public ResponseEntity<DashboardResponse> getDashboard() {
        DashboardMetrics metrics = adminService.getDashboard();
        return ResponseEntity.ok(DashboardResponse.fromDomain(metrics));
    }

    @GetMapping("/products")
    @Operation(summary = "Admin list all products")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<Product> products = adminService.getAllProducts();
        List<ProductResponse> response = products.stream()
                .map(p -> new ProductResponse(p.getId(), p.getName(), p.getDescription(),
                        p.getPrice(), p.getStock(), p.getImageUrl(), p.getSku(), p.getCategoryId()))
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/products/{id}")
    @Operation(summary = "Admin get product by ID")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        Product product = adminService.getProductById(id);
        ProductResponse response = new ProductResponse(product.getId(), product.getName(),
                product.getDescription(), product.getPrice(), product.getStock(),
                product.getImageUrl(), product.getSku(), product.getCategoryId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Admin create product")
    public ProductResponse createProduct(@RequestBody ProductRequest productRequest) {
        Product product = new Product(null, productRequest.getName(), productRequest.getDescription(),
                productRequest.getPrice(), productRequest.getStock(), productRequest.getImageUrl(),
                productRequest.getSku(), productRequest.getCategoryId());
        Product created = adminService.createProduct(product);
        return new ProductResponse(created.getId(), created.getName(), created.getDescription(),
                created.getPrice(), created.getStock(), created.getImageUrl(), created.getSku(),
                created.getCategoryId());
    }

    @PutMapping("/products/{id}")
    @Operation(summary = "Admin update product")
    public ProductResponse updateProduct(@PathVariable Long id, @RequestBody ProductRequest productRequest) {
        Product product = new Product(id, productRequest.getName(), productRequest.getDescription(),
                productRequest.getPrice(), productRequest.getStock(), productRequest.getImageUrl(),
                productRequest.getSku(), productRequest.getCategoryId());
        Product updated = adminService.updateProduct(id, product);
        return new ProductResponse(updated.getId(), updated.getName(), updated.getDescription(),
                updated.getPrice(), updated.getStock(), updated.getImageUrl(), updated.getSku(),
                updated.getCategoryId());
    }

    @DeleteMapping("/products/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Admin delete product")
    public void deleteProduct(@PathVariable Long id) {
        adminService.deleteProduct(id);
    }

    @GetMapping("/orders")
    @Operation(summary = "Admin list orders")
    public ResponseEntity<List<AdminOrderResponse>> getAllOrders(
            @RequestParam(required = false) OrderStatus status) {
        List<Order> orders = adminService.getAllOrders(status);
        List<AdminOrderResponse> response = orders.stream()
                .map(AdminOrderResponse::fromDomain)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    @Operation(summary = "Admin list users")
    public ResponseEntity<List<AdminUserResponse>> getAllUsers() {
        List<User> users = adminService.getAllUsers();
        List<AdminUserResponse> response = users.stream()
                .map(AdminUserResponse::fromDomain)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/{id}/role")
    @Operation(summary = "Admin update user role")
    public ResponseEntity<AdminUserResponse> updateUserRole(
            @PathVariable Long id,
            @RequestBody UpdateRoleRequest request) {
        UserRole newRole = UserRole.valueOf(request.getRole().toUpperCase());
        User updated = adminService.updateUserRole(id, newRole);
        return ResponseEntity.ok(AdminUserResponse.fromDomain(updated));
    }
}

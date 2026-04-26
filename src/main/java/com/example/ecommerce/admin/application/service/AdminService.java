package com.example.ecommerce.admin.application.service;

import com.example.ecommerce.admin.application.port.out.DashboardRepositoryPort;
import com.example.ecommerce.admin.domain.model.DashboardMetrics;
import com.example.ecommerce.order.application.port.out.OrderRepositoryPort;
import com.example.ecommerce.order.domain.model.Order;
import com.example.ecommerce.order.domain.model.OrderStatus;
import com.example.ecommerce.product.application.port.in.CreateProductUseCase;
import com.example.ecommerce.product.application.port.in.DeleteProductUseCase;
import com.example.ecommerce.product.application.port.in.GetProductUseCase;
import com.example.ecommerce.product.application.port.in.UpdateProductUseCase;
import com.example.ecommerce.product.domain.model.Product;
import com.example.ecommerce.user.application.port.out.UserRepositoryPort;
import com.example.ecommerce.user.domain.model.User;
import com.example.ecommerce.user.domain.model.UserRole;

import java.util.List;

public class AdminService {

    private final DashboardRepositoryPort dashboardRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final GetProductUseCase getProductUseCase;
    private final CreateProductUseCase createProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final DeleteProductUseCase deleteProductUseCase;

    public AdminService(DashboardRepositoryPort dashboardRepositoryPort,
                        OrderRepositoryPort orderRepositoryPort,
                        UserRepositoryPort userRepositoryPort,
                        GetProductUseCase getProductUseCase,
                        CreateProductUseCase createProductUseCase,
                        UpdateProductUseCase updateProductUseCase,
                        DeleteProductUseCase deleteProductUseCase) {
        this.dashboardRepositoryPort = dashboardRepositoryPort;
        this.orderRepositoryPort = orderRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.getProductUseCase = getProductUseCase;
        this.createProductUseCase = createProductUseCase;
        this.updateProductUseCase = updateProductUseCase;
        this.deleteProductUseCase = deleteProductUseCase;
    }

    public DashboardMetrics getDashboard() {
        return dashboardRepositoryPort.getMetrics();
    }

    public Product getProductById(Long id) {
        return getProductUseCase.getProductById(id);
    }

    public List<Product> getAllProducts() {
        return getProductUseCase.getAllProducts();
    }

    public Product createProduct(Product product) {
        return createProductUseCase.createProduct(product);
    }

    public Product updateProduct(Long id, Product product) {
        return updateProductUseCase.updateProduct(id, product);
    }

    public void deleteProduct(Long id) {
        deleteProductUseCase.deleteProduct(id);
    }

    public List<Order> getAllOrders(OrderStatus status) {
        if (status != null) {
            return orderRepositoryPort.findByStatus(status);
        }
        return orderRepositoryPort.findAll();
    }

    public List<User> getAllUsers() {
        return userRepositoryPort.findAll();
    }

    public User getUserById(Long id) {
        return userRepositoryPort.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    public User updateUserRole(Long userId, UserRole newRole) {
        User user = getUserById(userId);
        user.setRole(newRole);
        return userRepositoryPort.save(user);
    }
}

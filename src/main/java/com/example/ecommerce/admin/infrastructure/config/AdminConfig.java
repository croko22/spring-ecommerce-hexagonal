package com.example.ecommerce.admin.infrastructure.config;

import com.example.ecommerce.admin.application.port.out.DashboardRepositoryPort;
import com.example.ecommerce.admin.application.service.AdminService;
import com.example.ecommerce.order.application.port.out.OrderRepositoryPort;
import com.example.ecommerce.product.application.port.in.CreateProductUseCase;
import com.example.ecommerce.product.application.port.in.DeleteProductUseCase;
import com.example.ecommerce.product.application.port.in.GetProductUseCase;
import com.example.ecommerce.product.application.port.in.UpdateProductUseCase;
import com.example.ecommerce.user.application.port.out.UserRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminConfig {

    @Bean
    public AdminService adminService(DashboardRepositoryPort dashboardRepositoryPort,
                                     OrderRepositoryPort orderRepositoryPort,
                                     UserRepositoryPort userRepositoryPort,
                                     GetProductUseCase getProductUseCase,
                                     CreateProductUseCase createProductUseCase,
                                     UpdateProductUseCase updateProductUseCase,
                                     DeleteProductUseCase deleteProductUseCase) {
        return new AdminService(dashboardRepositoryPort, orderRepositoryPort, userRepositoryPort,
                getProductUseCase, createProductUseCase, updateProductUseCase, deleteProductUseCase);
    }
}

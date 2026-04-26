package com.example.ecommerce.admin.infrastructure.adapter.out.persistence;

import com.example.ecommerce.admin.application.port.out.DashboardRepositoryPort;
import com.example.ecommerce.admin.domain.model.DashboardMetrics;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

@Repository
public class DashboardRepositoryAdapter implements DashboardRepositoryPort {

    private final EntityManager entityManager;

    public DashboardRepositoryAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public DashboardMetrics getMetrics() {
        long totalProducts = countAll("products");
        long totalOrders = countAll("orders");
        long totalUsers = countAll("users");
        double totalRevenue = sumTotalAmount();

        return new DashboardMetrics(totalProducts, totalOrders, totalUsers, totalRevenue);
    }

    private long countAll(String tableName) {
        Query query = entityManager.createNativeQuery("SELECT COUNT(*) FROM " + tableName);
        Number result = (Number) query.getSingleResult();
        return result.longValue();
    }

    private double sumTotalAmount() {
        Query query = entityManager.createNativeQuery("SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE status = 'DELIVERED'");
        Number result = (Number) query.getSingleResult();
        return result.doubleValue();
    }
}

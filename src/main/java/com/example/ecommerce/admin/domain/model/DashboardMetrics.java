package com.example.ecommerce.admin.domain.model;

public class DashboardMetrics {

    private long totalProducts;
    private long totalOrders;
    private long totalUsers;
    private double totalRevenue;

    public DashboardMetrics(long totalProducts, long totalOrders, long totalUsers, double totalRevenue) {
        this.totalProducts = totalProducts;
        this.totalOrders = totalOrders;
        this.totalUsers = totalUsers;
        this.totalRevenue = totalRevenue;
    }

    public long getTotalProducts() {
        return totalProducts;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }
}

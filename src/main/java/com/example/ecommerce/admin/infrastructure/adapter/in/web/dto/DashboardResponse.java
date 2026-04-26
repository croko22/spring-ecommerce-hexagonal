package com.example.ecommerce.admin.infrastructure.adapter.in.web.dto;

import com.example.ecommerce.admin.domain.model.DashboardMetrics;

public class DashboardResponse {

    private long totalProducts;
    private long totalOrders;
    private long totalUsers;
    private double totalRevenue;

    public DashboardResponse() {
    }

    public static DashboardResponse fromDomain(DashboardMetrics metrics) {
        DashboardResponse response = new DashboardResponse();
        response.setTotalProducts(metrics.getTotalProducts());
        response.setTotalOrders(metrics.getTotalOrders());
        response.setTotalUsers(metrics.getTotalUsers());
        response.setTotalRevenue(metrics.getTotalRevenue());
        return response;
    }

    public long getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(long totalProducts) {
        this.totalProducts = totalProducts;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}

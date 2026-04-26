package com.example.ecommerce.admin.application.port.out;

import com.example.ecommerce.admin.domain.model.DashboardMetrics;

public interface DashboardRepositoryPort {

    DashboardMetrics getMetrics();
}

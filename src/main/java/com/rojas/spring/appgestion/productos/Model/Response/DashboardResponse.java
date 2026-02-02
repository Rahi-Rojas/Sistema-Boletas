package com.rojas.spring.appgestion.productos.Model.Response;

import lombok.Builder;

@Builder
public record DashboardResponse(
        Double totalRevenue,
        Long totalOrders,
        Long productsRunningLow
) {}
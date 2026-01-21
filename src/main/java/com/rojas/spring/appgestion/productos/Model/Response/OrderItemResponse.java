package com.rojas.spring.appgestion.productos.Model.Response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OrderItemResponse(
        Long id,
        String productName,
        Integer quantity,
        Double priceAtPurchase,
        Double subtotal,
        Boolean isActive
) {}
package com.rojas.spring.appgestion.productos.Model.Response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OrderResponse(
        Long id,
        LocalDateTime createdAt,
        Double total,
        String userName,
        List<OrderItemResponse> items,
        Boolean isActive
) {}
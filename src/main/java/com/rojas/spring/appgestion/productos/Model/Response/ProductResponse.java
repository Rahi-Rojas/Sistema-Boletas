package com.rojas.spring.appgestion.productos.Model.Response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductResponse(
        Long id,
        String name,
        String description,
        Double price,
        Integer stock,
        String imageUrl,
        Boolean isActive
) {}
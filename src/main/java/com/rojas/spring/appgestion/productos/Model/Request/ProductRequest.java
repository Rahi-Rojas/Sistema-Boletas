package com.rojas.spring.appgestion.productos.Model.Request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Data
public class ProductRequest {
    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    private String description;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.1")
    private Double price;

    @NotNull(message = "El stock es obligatorio")
    @Min(0)
    private Integer stock;

    private String imageUrl;
}

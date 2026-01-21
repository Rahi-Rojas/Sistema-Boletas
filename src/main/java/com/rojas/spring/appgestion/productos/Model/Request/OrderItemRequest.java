package com.rojas.spring.appgestion.productos.Model.Request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequest {
    @NotNull
    private Long productId;

    @NotNull @Min(1)
    private Integer quantity;
}
package com.rojas.spring.appgestion.productos.Model.Request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    @NotNull(message = "El ID de usuario es necesario")
    private Long userId;

    @NotEmpty(message = "La orden debe tener al menos un producto")
    private List<OrderItemRequest> items;

}
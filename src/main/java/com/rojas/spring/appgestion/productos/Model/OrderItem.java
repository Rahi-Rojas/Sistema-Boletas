package com.rojas.spring.appgestion.productos.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad mínima es 1")
    private Integer quantity;

    @NotNull(message = "El precio de compra es obligatorio")
    @DecimalMin(value = "0.1", message = "El precio debe ser mayor a 0")
    @Column(name = "price_at_purchase")
    private Double priceAtPurchase;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @NotNull(message = "Debe seleccionar un producto")
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
package com.rojas.spring.appgestion.productos.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La fecha de creación es obligatoria")
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @NotNull(message = "El total es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true)
    private Double total;

    @NotNull(message = "La boleta debe estar asociada a un usuario")
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
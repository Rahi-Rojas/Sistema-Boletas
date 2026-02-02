package com.rojas.spring.appgestion.productos.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Column(nullable = false)
    private String password;

    @Email(message = "Formato de email inválido")
    @NotBlank(message = "El email no puede estar vacio")
    private String email;

    @NotBlank
    private String role;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
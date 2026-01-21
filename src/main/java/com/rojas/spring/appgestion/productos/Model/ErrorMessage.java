package com.rojas.spring.appgestion.productos.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class ErrorMessage {
    private String message;
    private int statusCode;
    private LocalDateTime timestamp;
    private String path;
}
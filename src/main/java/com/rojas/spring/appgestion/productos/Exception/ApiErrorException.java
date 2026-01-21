package com.rojas.spring.appgestion.productos.Exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;

@Getter
public class ApiErrorException extends RuntimeException {

    private final String message;
    private final HttpStatus status;

    public ApiErrorException(String message, HttpStatus status) {
        super(message);
        this.message = message;
        this.status = status;
    }
}
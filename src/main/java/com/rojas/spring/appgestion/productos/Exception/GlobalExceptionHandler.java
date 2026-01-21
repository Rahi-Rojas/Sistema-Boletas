package com.rojas.spring.appgestion.productos.Exception;

import com.rojas.spring.appgestion.productos.Model.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;
import java.util.HashMap;
import java.util.Map;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Atrapa tus errores personalizados (ApiErrorException)
    @ExceptionHandler(ApiErrorException.class)
    public ResponseEntity<ErrorMessage> handleApiError(ApiErrorException ex, WebRequest request) {
        ErrorMessage error = ErrorMessage.builder()
                .message(ex.getMessage())
                .statusCode(ex.getStatus().value())
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .build();
        return new ResponseEntity<>(error, ex.getStatus());
    }

    // 2. Atrapa errores de lógica/stock (RuntimeException)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorMessage> handleRuntimeException(RuntimeException ex, WebRequest request) {
        ErrorMessage error = ErrorMessage.builder()
                .message(ex.getMessage())
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // 3. Atrapa CUALQUIER otra cosa que falle (El escudo final)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> handleGlobalException(Exception ex, WebRequest request) {
        ErrorMessage error = ErrorMessage.builder()
                .message("Error interno en el servidor: " + ex.getMessage())
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .build();
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

}
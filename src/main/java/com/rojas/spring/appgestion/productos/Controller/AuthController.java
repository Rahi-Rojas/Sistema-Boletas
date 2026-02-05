package com.rojas.spring.appgestion.productos.Controller;

import com.rojas.spring.appgestion.productos.Model.Request.LoginRequest;
import com.rojas.spring.appgestion.productos.Model.Response.UserResponse;
import com.rojas.spring.appgestion.productos.Service.JwtService;
import com.rojas.spring.appgestion.productos.Service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService; // <--- Inyecta el servicio que creamos

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        UserResponse user = userService.login(loginRequest);

        // Generamos el token
        String token = jwtService.generateToken(user.username());

        // AGREGAMOS "id" AL MAPA DE RESPUESTA
        return ResponseEntity.ok(Map.of(
                "token", token,
                "username", user.username(),
                "role", user.role(),
                "id", user.id() // <--- ¡ESTA ES LA LÍNEA MÁGICA!
        ));
    }
}
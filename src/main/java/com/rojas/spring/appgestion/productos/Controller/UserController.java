package com.rojas.spring.appgestion.productos.Controller;

import com.rojas.spring.appgestion.productos.Model.Request.UserRequest;
import com.rojas.spring.appgestion.productos.Model.Response.UserResponse;
import com.rojas.spring.appgestion.productos.Service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    // Obtiene todos los usuarios (Ahora incluye activos e inactivos por el cambio en el Service)
    @GetMapping("/find-all")
    public ResponseEntity<List<UserResponse>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @PostMapping("/create")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest userRequest) {
        return new ResponseEntity<>(userService.create(userRequest), HttpStatus.CREATED);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<UserResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest userRequest) {
        return ResponseEntity.ok(userService.update(id, userRequest));
    }

    // BORRADO LÓGICO: Desactiva al usuario (Protegido contra ADMIN en el Service)
    @DeleteMapping("/delete-soft/{id}")
    public ResponseEntity<Void> deleteSoft(@PathVariable Long id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // NUEVO - BORRADO FÍSICO: Elimina permanentemente de la BD
    // En una app real, este endpoint debería estar protegido solo para SUPER_ADMIN
    @DeleteMapping("/delete-hard/{id}")
    public ResponseEntity<Void> deleteHard(@PathVariable Long id) {
        // Asegúrate de haber agregado este método en tu Interfaz UserService
        userService.hardDeleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }
}
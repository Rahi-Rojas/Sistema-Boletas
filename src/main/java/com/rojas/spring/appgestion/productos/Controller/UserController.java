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

    @DeleteMapping("/delete-soft/{id}")
    public ResponseEntity<Void> deleteSoft(@PathVariable Long id) {
        // En tu ServiceImpl, el deleteById ya maneja la lógica de borrado
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }
}
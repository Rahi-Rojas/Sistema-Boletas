package com.rojas.spring.appgestion.productos.Controller;

import com.rojas.spring.appgestion.productos.Model.Request.OrderRequest;
import com.rojas.spring.appgestion.productos.Model.Response.OrderResponse;
import com.rojas.spring.appgestion.productos.Service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/find-all")
    public ResponseEntity<List<OrderResponse>> findAll() {
        return ResponseEntity.ok(orderService.findAll());
    }

    @PostMapping("/create")
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderRequest request) {
        return new ResponseEntity<>(orderService.create(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

}
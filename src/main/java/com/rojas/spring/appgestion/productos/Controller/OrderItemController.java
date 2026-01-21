package com.rojas.spring.appgestion.productos.Controller;

import com.rojas.spring.appgestion.productos.Model.Request.OrderItemRequest;
import com.rojas.spring.appgestion.productos.Model.Response.OrderItemResponse;
import com.rojas.spring.appgestion.productos.Service.OrderItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/order-item")
@RequiredArgsConstructor
public class OrderItemController {

    private final OrderItemService orderItemService;

    @GetMapping("/find-all")
    public ResponseEntity<List<OrderItemResponse>> findAll() {
        return ResponseEntity.ok(orderItemService.findAll());
    }

    @PostMapping("/create")
    public ResponseEntity<OrderItemResponse> create(@Valid @RequestBody OrderItemRequest request) {
        return new ResponseEntity<>(orderItemService.create(request), HttpStatus.CREATED);
    }
}

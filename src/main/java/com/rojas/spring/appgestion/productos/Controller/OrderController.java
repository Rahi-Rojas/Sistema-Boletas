package com.rojas.spring.appgestion.productos.Controller;

import com.rojas.spring.appgestion.productos.Model.Request.OrderRequest;
import com.rojas.spring.appgestion.productos.Model.Response.DashboardResponse;
import com.rojas.spring.appgestion.productos.Model.Response.OrderResponse;
import com.rojas.spring.appgestion.productos.Repository.OrderRepository;
import com.rojas.spring.appgestion.productos.Service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final com.rojas.spring.appgestion.productos.Repository.ProductRepository productRepository;

    @GetMapping("/find-all")
    public ResponseEntity<List<OrderResponse>> findAll() {
        return ResponseEntity.ok(orderService.findAll());
    }

    @GetMapping("/my-orders") // Ya no recibe {userId}
    public ResponseEntity<List<OrderResponse>> findMyOrders() {
        return ResponseEntity.ok(orderService.findMyOrders());
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        try {
            orderService.cancelOrder(id);
            return ResponseEntity.ok(Collections.singletonMap("message", "Orden cancelada y stock devuelto exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        } //todo de mi servicio lance la excepcion en el catch y mi global exception handler lo capta y muestra
    }

    @PostMapping("/create")
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderRequest request) {
        return new ResponseEntity<>(orderService.create(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    // Cambiamos el nombre del endpoint a algo más descriptivo
    @GetMapping("/reporte-ventas")
    public ResponseEntity<DashboardResponse> getReporteVentas() {
        Double revenue = orderRepository.getTotalRevenue();
        Long totalOrders = orderRepository.count();
        Long lowStock = productRepository.countByStockLessThan(5);

        DashboardResponse response = DashboardResponse.builder()
                .totalRevenue(revenue)
                .totalOrders(totalOrders)
                .productsRunningLow(lowStock)
                .build();

        return ResponseEntity.ok(response);
    }

}
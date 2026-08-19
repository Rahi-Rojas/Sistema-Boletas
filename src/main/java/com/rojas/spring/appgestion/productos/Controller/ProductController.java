package com.rojas.spring.appgestion.productos.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rojas.spring.appgestion.productos.Model.Request.ProductRequest;
import com.rojas.spring.appgestion.productos.Model.Response.ProductResponse;
import com.rojas.spring.appgestion.productos.Service.ProductService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @GetMapping("/find-all")
    public ResponseEntity<List<ProductResponse>> findAll() {
        return ResponseEntity.ok(productService.findAll());
    }

    // MEJORA: Imagen obligatoria para creación (required = true por defecto)
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> create(
            @RequestPart("request") String requestJson,
            @RequestPart("file") MultipartFile file) throws IOException {

        ProductRequest request = objectMapper.readValue(requestJson, ProductRequest.class);
        validate(request);

        return new ResponseEntity<>(productService.create(request, file), HttpStatus.CREATED);
    }

    @PutMapping(value = "/update/{id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<ProductResponse> update(
            @PathVariable Long id,
            @RequestPart("request") String requestJson,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {

        ProductRequest request = objectMapper.readValue(requestJson, ProductRequest.class);
        validate(request);

        return ResponseEntity.ok(productService.update(id, request, file));
    }

    // SOFT DELETE: Desactivar producto (Borrado lógico)
    @DeleteMapping("/delete-soft/{id}")
    public ResponseEntity<Void> deleteSoft(@PathVariable Long id) {
        productService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // HARD DELETE: Eliminar permanentemente (Borrado físico)
    @DeleteMapping("/delete-hard/{id}")
    public ResponseEntity<Void> deleteHard(@PathVariable Long id) {
        productService.hardDeleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> patch(
            @PathVariable Long id,
            @RequestPart(value = "request", required = false) String requestJson,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {

        ProductRequest request = null;
        if (requestJson != null && !requestJson.isBlank() && !requestJson.equalsIgnoreCase("string")) {
            request = objectMapper.readValue(requestJson, ProductRequest.class);
            validate(request);
        }

        return ResponseEntity.ok(productService.patch(id, request, file));
    }

    private void validate(ProductRequest request) {
        Set<ConstraintViolation<ProductRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @GetMapping("/findByPriceRange")
    public ResponseEntity<List<ProductResponse>> findByPriceRange(
            @RequestParam Double minPrice,
            @RequestParam Double maxPrice) {
        return ResponseEntity.ok(productService.findByPriceRange(minPrice, maxPrice));
    }
}
package com.rojas.spring.appgestion.productos;

import com.rojas.spring.appgestion.productos.Mapper.ProductMapper;
import com.rojas.spring.appgestion.productos.Model.Product;
import com.rojas.spring.appgestion.productos.Model.Request.ProductRequest;
import com.rojas.spring.appgestion.productos.Model.Response.ProductResponse;
import com.rojas.spring.appgestion.productos.Repository.ProductRepository;
import com.rojas.spring.appgestion.productos.Service.impl.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper; // También necesitamos simular el mapper

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void cuandoCrearProducto_entoncesRetornarProductResponse() {
        // 1. ARRANGE
        ProductRequest request = new ProductRequest();
        request.setName("Laptop Gaming");
        request.setPrice(1500.0);

        Product productoEntidad = new Product();
        productoEntidad.setName("Laptop Gaming");

        // Usamos el Builder del Record (esto elimina el error de los parámetros)
        ProductResponse responseEsperada = ProductResponse.builder()
                .id(1L)
                .name("Laptop Gaming")
                .price(1500.0)
                .build();

        when(productMapper.toEntity(any(ProductRequest.class))).thenReturn(productoEntidad);
        when(productRepository.save(any(Product.class))).thenReturn(productoEntidad);
        when(productMapper.toResponse(any(Product.class))).thenReturn(responseEsperada);

        // 2. ACT
        ProductResponse resultado = productService.create(request);

        // 3. ASSERT
        assertNotNull(resultado);
        // IMPORTANTE: En los records no se usa .getName(), se usa .name()
        assertEquals("Laptop Gaming", resultado.name());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void cuandoBorrarIdNoExistente_entoncesLanzarExcepcion() {
        // 1. ARRANGE
        Long idInexistente = 99L;

        // Simulamos que el repositorio devuelve un Optional vacío
        when(productRepository.findById(idInexistente)).thenReturn(java.util.Optional.empty());

        // 2. ACT & ASSERT (Todo en uno para excepciones)
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.deleteById(idInexistente);
        });

        // 3. VERIFY
        assertEquals("Producto no encontrado con ID: 99", exception.getMessage());
        // Verificamos que el repositorio NUNCA intentó guardar (borrado lógico) porque falló antes
        verify(productRepository, never()).save(any(Product.class));
    }

}
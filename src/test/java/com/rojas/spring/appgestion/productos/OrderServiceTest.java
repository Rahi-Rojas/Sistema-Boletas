package com.rojas.spring.appgestion.productos;

import com.rojas.spring.appgestion.productos.Mapper.OrderMapper;
import com.rojas.spring.appgestion.productos.Model.*;
import com.rojas.spring.appgestion.productos.Model.Request.*;
import com.rojas.spring.appgestion.productos.Model.Response.OrderResponse;
import com.rojas.spring.appgestion.productos.Repository.*;
import com.rojas.spring.appgestion.productos.Service.impl.OrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderMapper orderMapper;
    @Mock private UserRepository userRepository;
    @Mock private ProductRepository productRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void cuandoCrearPedidoConStock_entoncesExito() {
        // 1. ARRANGE
        OrderRequest request = new OrderRequest();
        request.setUserId(1L);

        OrderItemRequest itemReq = new OrderItemRequest();
        itemReq.setProductId(10L);
        itemReq.setQuantity(2);
        request.setItems(List.of(itemReq));

        Product mockProduct = new Product();
        mockProduct.setId(10L);
        mockProduct.setPrice(100.0);

        // Simulaciones clave
        when(orderMapper.toEntity(any())).thenReturn(new Order());
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));

        // Simulamos que el stock se descuenta correctamente (devuelve 1 fila afectada)
        when(productRepository.decreaseStock(10L, 2)).thenReturn(1);
        when(productRepository.findById(10L)).thenReturn(Optional.of(mockProduct));

        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderMapper.toResponse(any())).thenReturn(OrderResponse.builder().total(200.0).build());

        // 2. ACT
        OrderResponse response = orderService.create(request);

        // 3. ASSERT
        assertNotNull(response);
        assertEquals(200.0, response.total());
        verify(productRepository, times(1)).decreaseStock(10L, 2);
        verify(orderRepository, times(1)).save(any());
    }

    @Test
    void cuandoCrearPedidoSinStock_entoncesLanzarExcepcion() {
        // 1. ARRANGE
        OrderRequest request = new OrderRequest();
        request.setUserId(1L);
        OrderItemRequest itemReq = new OrderItemRequest();
        itemReq.setProductId(10L);
        itemReq.setQuantity(100); // Mucha cantidad
        request.setItems(List.of(itemReq));

        when(orderMapper.toEntity(any())).thenReturn(new Order());
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));

        // Simulamos que NO hay stock (devuelve 0 filas afectadas)
        when(productRepository.decreaseStock(10L, 100)).thenReturn(0);

        // 2. ACT & ASSERT
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            orderService.create(request);
        });

        assertTrue(ex.getMessage().contains("Stock insuficiente"));
        verify(orderRepository, never()).save(any()); // No debe guardar nada si falló el stock
    }
}
package com.rojas.spring.appgestion.productos;

import com.rojas.spring.appgestion.productos.Exception.ApiErrorException;
import com.rojas.spring.appgestion.productos.Mapper.OrderMapper;
import com.rojas.spring.appgestion.productos.Model.*;
import com.rojas.spring.appgestion.productos.Model.Request.*;
import com.rojas.spring.appgestion.productos.Model.Response.OrderResponse;
import com.rojas.spring.appgestion.productos.Repository.*;
import com.rojas.spring.appgestion.productos.Service.impl.OrderServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

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

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    private void loginAs(String username, String role) {
        loginAs(username, role, 1L);
    }

    private void loginAs(String username, String role, Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRole(role);
        lenient().when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        var auth = new UsernamePasswordAuthenticationToken(
                username, null, List.of(new SimpleGrantedAuthority(role)));
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    private OrderRequest crearRequest(Long userId, Long productId, int cantidad) {
        OrderRequest request = new OrderRequest();
        request.setUserId(userId);

        OrderItemRequest itemReq = new OrderItemRequest();
        itemReq.setProductId(productId);
        itemReq.setQuantity(cantidad);
        request.setItems(List.of(itemReq));
        return request;
    }

    private void mockStockExitoso(Long productId, double precio) {
        Product mockProduct = new Product();
        mockProduct.setId(productId);
        mockProduct.setPrice(precio);
        when(productRepository.decreaseStock(productId, 2)).thenReturn(1);
        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));
    }

    @Test
    void cuandoCrearPedidoConStock_entoncesExito() {
        loginAs("cliente", "USER");
        OrderRequest request = crearRequest(1L, 10L, 2);

        when(orderMapper.toEntity(any())).thenReturn(new Order());
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        mockStockExitoso(10L, 100.0);

        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderMapper.toResponse(any())).thenReturn(OrderResponse.builder().total(200.0).build());

        OrderResponse response = orderService.create(request);

        assertNotNull(response);
        assertEquals(200.0, response.total());
        verify(productRepository, times(1)).decreaseStock(10L, 2);
        verify(orderRepository, times(1)).save(any());
    }

    @Test
    void cuandoCrearPedidoSinStock_entoncesLanzarExcepcion() {
        loginAs("cliente", "USER");
        OrderRequest request = crearRequest(1L, 10L, 100);

        when(orderMapper.toEntity(any())).thenReturn(new Order());
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));

        // Simulamos que NO hay stock (devuelve 0 filas afectadas)
        when(productRepository.decreaseStock(10L, 100)).thenReturn(0);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            orderService.create(request);
        });

        assertTrue(ex.getMessage().contains("Stock insuficiente"));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void cuandoUsuarioEnviaUserIdAjeno_entoncesSeUsaSuPropioId() {
        loginAs("cliente", "USER");
        OrderRequest request = crearRequest(999L, 10L, 2); // intenta crear a nombre de otro

        when(orderMapper.toEntity(any())).thenReturn(new Order());
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        mockStockExitoso(10L, 50.0);
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderMapper.toResponse(any())).thenReturn(OrderResponse.builder().build());

        orderService.create(request);

        ArgumentCaptor<OrderRequest> captor = ArgumentCaptor.forClass(OrderRequest.class);
        verify(orderMapper).toEntity(captor.capture());
        assertEquals(1L, captor.getValue().getUserId(), "El userId debe forzarse al del usuario autenticado");
        verify(userRepository, never()).findById(999L);
    }

    @Test
    void cuandoAdminCreaPedidoParaOtroUsuario_entoncesRespetaUserId() {
        loginAs("admin", "ADMIN");
        OrderRequest request = crearRequest(5L, 10L, 2);

        when(orderMapper.toEntity(any())).thenReturn(new Order());
        when(userRepository.findById(5L)).thenReturn(Optional.of(new User()));
        mockStockExitoso(10L, 50.0);
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderMapper.toResponse(any())).thenReturn(OrderResponse.builder().build());

        orderService.create(request);

        ArgumentCaptor<OrderRequest> captor = ArgumentCaptor.forClass(OrderRequest.class);
        verify(orderMapper).toEntity(captor.capture());
        assertEquals(5L, captor.getValue().getUserId(), "El ADMIN debe poder elegir el usuario");
        verify(userRepository).findById(5L);
    }

    @Test
    void cuandoVerOrdenPropia_entoncesExito() {
        loginAs("cliente", "USER");
        User owner = new User();
        owner.setId(1L);
        Order order = new Order();
        order.setId(100L);
        order.setUser(owner);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(OrderResponse.builder().id(100L).build());

        OrderResponse response = orderService.findById(100L);

        assertEquals(100L, response.id());
    }

    @Test
    void cuandoVerOrdenAjena_entoncesLanzar403() {
        loginAs("cliente", "USER");
        User other = new User();
        other.setId(2L);
        Order order = new Order();
        order.setId(100L);
        order.setUser(other);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        ApiErrorException ex = assertThrows(ApiErrorException.class, () -> orderService.findById(100L));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }

    @Test
    void cuandoVerOrdenInexistente_entoncesLanzar404() {
        loginAs("cliente", "USER");
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        ApiErrorException ex = assertThrows(ApiErrorException.class, () -> orderService.findById(99L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void cuandoCancelarOrdenPropia_entoncesDevuelveStock() {
        loginAs("cliente", "USER");
        Product product = new Product();
        product.setId(10L);
        product.setStock(5);

        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(2);

        User owner = new User();
        owner.setId(1L);
        Order order = new Order();
        order.setId(100L);
        order.setUser(owner);
        order.setIsActive(true);
        order.setItems(List.of(item));

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        orderService.cancelOrder(100L);

        assertEquals(7, product.getStock(), "El stock debe devolverse al cancelar");
        assertFalse(order.getIsActive());
        verify(productRepository, times(1)).save(product);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void cuandoCancelarOrdenAjena_entoncesLanzar403() {
        loginAs("cliente", "USER");
        User other = new User();
        other.setId(2L);
        Order order = new Order();
        order.setId(100L);
        order.setUser(other);
        order.setIsActive(true);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        ApiErrorException ex = assertThrows(ApiErrorException.class, () -> orderService.cancelOrder(100L));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        verify(orderRepository, never()).save(order);
    }

    @Test
    void cuandoAdminCancelarOrdenDeOtro_entoncesExito() {
        loginAs("admin", "ADMIN");
        Product product = new Product();
        product.setId(10L);
        product.setStock(3);

        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(1);

        User other = new User();
        other.setId(2L);
        Order order = new Order();
        order.setId(100L);
        order.setUser(other);
        order.setIsActive(true);
        order.setItems(List.of(item));

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        orderService.cancelOrder(100L);

        assertEquals(4, product.getStock());
        assertFalse(order.getIsActive());
    }
}

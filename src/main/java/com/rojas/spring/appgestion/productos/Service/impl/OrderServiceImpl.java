package com.rojas.spring.appgestion.productos.Service.impl;

import com.rojas.spring.appgestion.productos.Exception.ApiErrorException;
import com.rojas.spring.appgestion.productos.Mapper.OrderMapper;
import com.rojas.spring.appgestion.productos.Model.Order;
import com.rojas.spring.appgestion.productos.Model.OrderItem;
import com.rojas.spring.appgestion.productos.Model.Product;
import com.rojas.spring.appgestion.productos.Model.Request.OrderRequest;
import com.rojas.spring.appgestion.productos.Model.Response.OrderResponse;
import com.rojas.spring.appgestion.productos.Model.User;
import com.rojas.spring.appgestion.productos.Repository.OrderRepository;
import com.rojas.spring.appgestion.productos.Repository.UserRepository;
import com.rojas.spring.appgestion.productos.Repository.ProductRepository;
import com.rojas.spring.appgestion.productos.Service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderServiceImpl
        extends BaseGenericServiceImpl<Order, OrderRequest, OrderResponse, Long, OrderRepository>
        implements OrderService {

    private final OrderMapper orderMapper;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    // El constructor debe tener TODOS estos parámetros para que Spring los inyecte
    public OrderServiceImpl(OrderRepository repository,
                            OrderMapper orderMapper,
                            UserRepository userRepository,
                            ProductRepository productRepository) {
        super(repository);
        this.orderMapper = orderMapper;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = repository;
    }

    @Override
    public List<OrderResponse> findMyOrders() {
        // 1. Obtener el username desde el token (SecurityContext)
        User user = currentUser();
        // 2. Retornar solo sus órdenes
        return orderRepository.findByUserId(user.getId())
                .stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public OrderResponse create(OrderRequest request) {
        User currentUser = currentUser();

        // Un usuario normal solo puede crear órdenes a su propio nombre.
        // Un ADMIN sí puede crear órdenes para cualquier usuario.
        if (!isAdmin()) {
            request.setUserId(currentUser.getId());
        }

        // 1. Mapeo inicial
        Order order = orderMapper.toEntity(request);

        // 2. Asignar Usuario y Fecha
        var user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + request.getUserId()));
        order.setUser(user);
        order.setCreatedAt(java.time.LocalDateTime.now());

        double totalAcumulado = 0;
        java.util.List<OrderItem> nuevosItems = new java.util.ArrayList<>();

        // 3. Procesar Items y descontar Stock de forma atómica
        if (request.getItems() != null) {
            for (var itemReq : request.getItems()) {

                // Intentamos descontar el stock directamente en la BD
                int filasActualizadas = productRepository.decreaseStock(itemReq.getProductId(), itemReq.getQuantity());

                // Si devuelve 0, es porque no hay stock o el ID no existe
                if (filasActualizadas == 0) {
                    throw new RuntimeException("Error: Stock insuficiente para el producto ID: " + itemReq.getProductId());
                }

                // Buscamos el producto para obtener el precio actual
                Product productBD = productRepository.findById(itemReq.getProductId())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

                // Creamos el Item de la orden
                OrderItem nuevoItem = new OrderItem();
                nuevoItem.setProduct(productBD);
                nuevoItem.setQuantity(itemReq.getQuantity());
                nuevoItem.setPriceAtPurchase(productBD.getPrice());
                nuevoItem.setOrder(order);
                nuevoItem.setIsActive(true);

                totalAcumulado += (productBD.getPrice() * itemReq.getQuantity());
                nuevosItems.add(nuevoItem);
            }
        }

        // 4. Finalizar y Guardar
        order.setItems(nuevosItems);
        order.setTotal(totalAcumulado);

        Order savedOrder = repository.save(order);
        return orderMapper.toResponse(savedOrder);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiErrorException("Orden no encontrada con ID: " + orderId, HttpStatus.NOT_FOUND));

        if (!isAdmin() && !order.getUser().getId().equals(currentUser().getId())) {
            throw new ApiErrorException("No puedes cancelar una orden que no te pertenece", HttpStatus.FORBIDDEN);
        }

        if (!order.getIsActive()) {
            throw new ApiErrorException("La orden ya está cancelada.", HttpStatus.BAD_REQUEST);
        }

        // Devolver el stock a cada producto
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }

        order.setIsActive(false);
        orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ApiErrorException("No se encontró la orden con el ID: " + id, HttpStatus.NOT_FOUND));

        if (!isAdmin() && !order.getUser().getId().equals(currentUser().getId())) {
            throw new ApiErrorException("No puedes ver una orden que no te pertenece", HttpStatus.FORBIDDEN);
        }

        return orderMapper.toResponse(order);
    }

    private User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new ApiErrorException("Usuario no autenticado", HttpStatus.UNAUTHORIZED);
        }
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ApiErrorException("Usuario no encontrado", HttpStatus.UNAUTHORIZED));
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ADMIN".equals(a.getAuthority()));
    }


    @Override
    @Transactional(readOnly = true)
    public java.util.List<OrderResponse> findAll() {
        // Usamos el 'repository' que ya viene de la clase BaseGeneric
        return repository.findAll().stream()
                .map(orderMapper::toResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    protected OrderResponse mapToResponse(Order entity) {
        return orderMapper.toResponse(entity);
    }

    @Override
    protected Order mapToEntity(OrderRequest request) {
        return orderMapper.toEntity(request);
    }
}
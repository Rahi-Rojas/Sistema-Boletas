package com.rojas.spring.appgestion.productos.Service.impl;

import com.rojas.spring.appgestion.productos.Mapper.OrderMapper;
import com.rojas.spring.appgestion.productos.Model.Order;
import com.rojas.spring.appgestion.productos.Model.OrderItem;
import com.rojas.spring.appgestion.productos.Model.Product;
import com.rojas.spring.appgestion.productos.Model.Request.OrderRequest;
import com.rojas.spring.appgestion.productos.Model.Response.OrderResponse;
import com.rojas.spring.appgestion.productos.Repository.OrderRepository;
import com.rojas.spring.appgestion.productos.Repository.UserRepository;
import com.rojas.spring.appgestion.productos.Repository.ProductRepository;
import com.rojas.spring.appgestion.productos.Service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderServiceImpl
        extends BaseGenericServiceImpl<Order, OrderRequest, OrderResponse, Long, OrderRepository>
        implements OrderService {

    private final OrderMapper orderMapper;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    // El constructor debe tener TODOS estos parámetros para que Spring los inyecte
    public OrderServiceImpl(OrderRepository repository,
                            OrderMapper orderMapper,
                            UserRepository userRepository,
                            ProductRepository productRepository) {
        super(repository);
        this.orderMapper = orderMapper;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public OrderResponse create(OrderRequest request) {
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
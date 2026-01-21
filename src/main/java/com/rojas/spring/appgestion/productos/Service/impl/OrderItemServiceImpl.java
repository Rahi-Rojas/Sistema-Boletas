package com.rojas.spring.appgestion.productos.Service.impl;

import com.rojas.spring.appgestion.productos.Mapper.OrderItemMapper;
import com.rojas.spring.appgestion.productos.Model.OrderItem;
import com.rojas.spring.appgestion.productos.Model.Request.OrderItemRequest;
import com.rojas.spring.appgestion.productos.Model.Response.OrderItemResponse;
import com.rojas.spring.appgestion.productos.Repository.OrderItemRepository;
import com.rojas.spring.appgestion.productos.Service.OrderItemService;
import org.springframework.stereotype.Service;

@Service
public class OrderItemServiceImpl
        extends BaseGenericServiceImpl<OrderItem, OrderItemRequest, OrderItemResponse, Long, OrderItemRepository>
        implements OrderItemService {

    private final OrderItemMapper orderItemMapper;

    public OrderItemServiceImpl(OrderItemRepository repository, OrderItemMapper orderItemMapper) {
        super(repository);
        this.orderItemMapper = orderItemMapper;
    }

    @Override
    protected OrderItemResponse mapToResponse(OrderItem entity) {
        return orderItemMapper.toResponse(entity);
    }

    @Override
    protected OrderItem mapToEntity(OrderItemRequest request) {
        return orderItemMapper.toEntity(request);
    }
}
package com.rojas.spring.appgestion.productos.Mapper;

import com.rojas.spring.appgestion.productos.Model.OrderItem;
import com.rojas.spring.appgestion.productos.Model.Request.OrderItemRequest;
import com.rojas.spring.appgestion.productos.Model.Response.OrderItemResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "product", ignore = true)
    OrderItem toEntity(OrderItemRequest request);

    @Mapping(target = "productName", source = "product.name")
    // Agregamos esta línea para asegurar que el precio se vea en la respuesta
    @Mapping(target = "priceAtPurchase", source = "product.price")
    @Mapping(target = "subtotal", expression = "java(entity.getQuantity() * entity.getProduct().getPrice())")
    OrderItemResponse toResponse(OrderItem entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromRequest(OrderItemRequest request, @MappingTarget OrderItem entity);
}
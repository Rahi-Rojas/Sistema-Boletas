package com.rojas.spring.appgestion.productos.Mapper;

import com.rojas.spring.appgestion.productos.Model.Order;
import com.rojas.spring.appgestion.productos.Model.Request.OrderRequest;
import com.rojas.spring.appgestion.productos.Model.Response.OrderResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "user", ignore = true) // El usuario se asigna en el Service
    Order toEntity(OrderRequest request);

    @Mapping(target = "userName", source = "user.username") // Mapeo directo del nombre
    OrderResponse toResponse(Order entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromRequest(OrderRequest request, @MappingTarget Order entity);
}
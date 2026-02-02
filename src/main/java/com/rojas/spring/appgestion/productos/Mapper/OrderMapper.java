package com.rojas.spring.appgestion.productos.Mapper;

import com.rojas.spring.appgestion.productos.Model.Order;
import com.rojas.spring.appgestion.productos.Model.Request.OrderRequest;
import com.rojas.spring.appgestion.productos.Model.Response.OrderResponse;
import org.mapstruct.*;

// todo: Agregamos 'uses' para que sepa mapear los items de la lista
@Mapper(componentModel = "spring", uses = {OrderItemMapper.class})
public interface OrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "user", ignore = true)
    Order toEntity(OrderRequest request);

    @Mapping(target = "userName", source = "user.username")
    OrderResponse toResponse(Order entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromRequest(OrderRequest request, @MappingTarget Order entity);
}
package com.rojas.spring.appgestion.productos.Mapper;

import com.rojas.spring.appgestion.productos.Model.Product;
import com.rojas.spring.appgestion.productos.Model.Request.ProductRequest;
import com.rojas.spring.appgestion.productos.Model.Response.ProductResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    Product toEntity(ProductRequest request);

    ProductResponse toResponse(Product entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromRequest(ProductRequest request, @MappingTarget Product entity);
}
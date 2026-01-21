package com.rojas.spring.appgestion.productos.Mapper;

import com.rojas.spring.appgestion.productos.Model.User;
import com.rojas.spring.appgestion.productos.Model.Request.UserRequest;
import com.rojas.spring.appgestion.productos.Model.Response.UserResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    User toEntity(UserRequest request);

    UserResponse toResponse(User entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromRequest(UserRequest request, @MappingTarget User entity);
}
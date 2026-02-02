package com.rojas.spring.appgestion.productos.Service;


import com.rojas.spring.appgestion.productos.Model.Request.LoginRequest;
import com.rojas.spring.appgestion.productos.Model.Request.UserRequest;
import com.rojas.spring.appgestion.productos.Model.Response.UserResponse;

public interface UserService
        extends BaseGenericService<UserRequest, UserResponse, Long>{
    UserResponse login(LoginRequest loginRequest);
    void hardDeleteById(Long id);
}

package com.rojas.spring.appgestion.productos.Service;


import com.rojas.spring.appgestion.productos.Model.Request.UserRequest;
import com.rojas.spring.appgestion.productos.Model.Response.UserResponse;

public interface UserService
        extends BaseGenericService<UserRequest, UserResponse, Long>{}

package com.rojas.spring.appgestion.productos.Service;

import com.rojas.spring.appgestion.productos.Model.Request.OrderRequest;
import com.rojas.spring.appgestion.productos.Model.Response.OrderResponse;

import java.util.List;

public interface OrderService
        extends BaseGenericService<OrderRequest, OrderResponse, Long>{
        List<OrderResponse> findAll();
}

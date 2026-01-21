package com.rojas.spring.appgestion.productos.Service;

import com.rojas.spring.appgestion.productos.Model.Request.OrderItemRequest;
import com.rojas.spring.appgestion.productos.Model.Response.OrderItemResponse;

public interface OrderItemService
        extends BaseGenericService<OrderItemRequest, OrderItemResponse, Long>{}

package com.rojas.spring.appgestion.productos.Service;

import com.rojas.spring.appgestion.productos.Model.Request.ProductRequest;
import com.rojas.spring.appgestion.productos.Model.Response.ProductResponse;

public interface ProductService
        extends BaseGenericService<ProductRequest, ProductResponse, Long>{}

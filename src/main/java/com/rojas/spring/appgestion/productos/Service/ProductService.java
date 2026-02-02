package com.rojas.spring.appgestion.productos.Service;

import com.rojas.spring.appgestion.productos.Model.Request.ProductRequest;
import com.rojas.spring.appgestion.productos.Model.Response.ProductResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ProductService
        extends BaseGenericService<ProductRequest, ProductResponse, Long>{
    ProductResponse create(ProductRequest request, MultipartFile file) throws java.io.IOException;
    ProductResponse update(Long id, ProductRequest request, MultipartFile file) throws java.io.IOException;
    ProductResponse patch(Long id, ProductRequest request, MultipartFile file) throws java.io.IOException;
    void hardDeleteById(Long id);
}

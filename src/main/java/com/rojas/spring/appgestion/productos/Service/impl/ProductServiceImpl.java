package com.rojas.spring.appgestion.productos.Service.impl;

import com.rojas.spring.appgestion.productos.Mapper.ProductMapper;
import com.rojas.spring.appgestion.productos.Model.Product;
import com.rojas.spring.appgestion.productos.Model.Request.ProductRequest;
import com.rojas.spring.appgestion.productos.Model.Response.ProductResponse;
import com.rojas.spring.appgestion.productos.Repository.ProductRepository;
import com.rojas.spring.appgestion.productos.Service.ProductService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl
        extends BaseGenericServiceImpl<Product, ProductRequest, ProductResponse, Long, ProductRepository>
        implements ProductService {

    private final ProductMapper productMapper;
    private final ProductRepository productRepository; // Lo añadimos para usar métodos específicos

    public ProductServiceImpl(ProductRepository repository, ProductMapper productMapper) {
        super(repository);
        this.productRepository = repository;
        this.productMapper = productMapper;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

        product.setIsActive(false); // Borrado lógico
        productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream()
                .filter(Product::getIsActive) // Filtramos solo los que están en true
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    protected ProductResponse mapToResponse(Product entity) {
        return productMapper.toResponse(entity);
    }

    @Override
    protected Product mapToEntity(ProductRequest request) {
        return productMapper.toEntity(request);
    }
}
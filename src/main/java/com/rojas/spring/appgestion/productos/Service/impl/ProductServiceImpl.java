package com.rojas.spring.appgestion.productos.Service.impl;

import com.rojas.spring.appgestion.productos.Exception.ApiErrorException;
import com.rojas.spring.appgestion.productos.Mapper.ProductMapper;
import com.rojas.spring.appgestion.productos.Model.Product;
import com.rojas.spring.appgestion.productos.Model.Request.ProductRequest;
import com.rojas.spring.appgestion.productos.Model.Response.ProductResponse;
import com.rojas.spring.appgestion.productos.Repository.ProductRepository;
import com.rojas.spring.appgestion.productos.Service.ProductService;
import com.rojas.spring.appgestion.productos.Service.UploadFileService; // Nuevo import
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl
        extends BaseGenericServiceImpl<Product, ProductRequest, ProductResponse, Long, ProductRepository>
        implements ProductService {

    private final ProductMapper productMapper;
    private final ProductRepository productRepository;
    private final UploadFileService uploadFileService; // 1. Añadimos el servicio de archivos

    // 2. Actualizamos el constructor para la inyección
    public ProductServiceImpl(ProductRepository repository, ProductMapper productMapper, UploadFileService uploadFileService) {
        super(repository);
        this.productRepository = repository;
        this.productMapper = productMapper;
        this.uploadFileService = uploadFileService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream()
                .filter(Product::getIsActive)
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request, MultipartFile file) throws IOException {
        // 1. Mapeamos el request a la entidad
        Product entity = productMapper.toEntity(request);
        entity.setIsActive(true);

        // 2. Si viene un archivo, lo guardamos y seteamos la URL
        if (file != null && !file.isEmpty()) {
            String fileName = uploadFileService.saveFile(file);
            entity.setImageUrl("http://localhost:8081/uploads/" + fileName);
        }

        // 3. Guardamos y retornamos
        Product savedEntity = productRepository.save(entity);
        return productMapper.toResponse(savedEntity);
    }

    @Override
    public void deleteById(Long id) {
        // SOFT DELETE: Borrado lógico
        productRepository.findById(id).ifPresentOrElse(product -> {
            // Cambiamos el estado a falso para que no aparezca en la tienda
            product.setIsActive(false);
            productRepository.save(product);
        }, () -> {
            throw new ApiErrorException("Producto no encontrado para desactivar", HttpStatus.NOT_FOUND);
        });
    }

    @Override
    public void hardDeleteById(Long id) {
        // HARD DELETE: Eliminación física de la base de datos
        if (!productRepository.existsById(id)) {
            throw new ApiErrorException("Producto no encontrado para eliminación física", HttpStatus.NOT_FOUND);
        }

        try {
            productRepository.deleteById(id);
        } catch (Exception e) {
            // Si el producto ya está en una orden de compra, el Hard Delete fallará por integridad referencial
            throw new ApiErrorException(
                    "No se puede eliminar físicamente: el producto tiene historial de ventas relacionado",
                    HttpStatus.CONFLICT
            );
        }
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductRequest request, MultipartFile file) throws IOException {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

        if (file != null && !file.isEmpty()) {
            Optional.ofNullable(product.getImageUrl()).ifPresent(uploadFileService::deleteFile);
            product.setImageUrl("http://localhost:8081/uploads/" + uploadFileService.saveFile(file));
        }

        updateFields(product, request); // Ya no saldrá en rojo
        return productMapper.toResponse(productRepository.save(product));
    }

    // ESTE ES EL MÉTODO QUE TE FALTABA ABAJO
    private void updateFields(Product p, ProductRequest r) {
        p.setName(r.getName());
        p.setDescription(r.getDescription());
        p.setPrice(r.getPrice());
        p.setStock(r.getStock());
        Optional.ofNullable(r.getIsActive()).ifPresent(p::setIsActive);
    }

    @Override
    @Transactional
    public ProductResponse patch(Long id, ProductRequest request, MultipartFile file) throws IOException {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró el producto con ID: " + id));

        // 1. Lógica para el archivo físico (si el usuario sube una imagen nueva)
        if (file != null && !file.isEmpty()) {
            // Borramos la imagen anterior del disco si existe
            Optional.ofNullable(product.getImageUrl()).ifPresent(uploadFileService::deleteFile);
            // Guardamos la nueva y actualizamos la URL
            product.setImageUrl("http://localhost:8081/uploads/" + uploadFileService.saveFile(file));
        }

        // 2. Lógica para los campos del JSON (solo actualiza si no son nulos)
        if (request != null) {
            Optional.ofNullable(request.getName()).ifPresent(product::setName);
            Optional.ofNullable(request.getDescription()).ifPresent(product::setDescription);
            Optional.ofNullable(request.getPrice()).ifPresent(product::setPrice);
            Optional.ofNullable(request.getStock()).ifPresent(product::setStock);
            Optional.ofNullable(request.getIsActive()).ifPresent(product::setIsActive);

            // Por si mandan una URL manual en lugar de un archivo
            Optional.ofNullable(request.getImageUrl()).ifPresent(product::setImageUrl);
        }

        return productMapper.toResponse(productRepository.save(product));
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
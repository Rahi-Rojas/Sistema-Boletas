package com.rojas.spring.appgestion.productos.Repository;

import com.rojas.spring.appgestion.productos.Model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock - :quantity WHERE p.id = :id AND p.stock >= :quantity")
    int decreaseStock(@Param("id") Long id, @Param("quantity") Integer quantity); //todo el param no es necesario
    //todo el metodo de arriba es para que se reste el stock de manera sincronizada
    //todo Este cuenta cuántos productos tienen stock por debajo del límite que le pases
    long countByStockLessThan(Integer stock);
}

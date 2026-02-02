package com.rojas.spring.appgestion.productos.Repository;

import com.rojas.spring.appgestion.productos.Model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT COALESCE(SUM(o.total), 0.0) FROM Order o")
    Double getTotalRevenue();
    //todo: nuevo metodo que cree para los usuario solo vean sus ordenes
    List<Order> findByUserId(Long userId);
}

package com.rojas.spring.appgestion.productos.Repository;

import com.rojas.spring.appgestion.productos.Model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}

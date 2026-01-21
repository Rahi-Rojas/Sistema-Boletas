package com.rojas.spring.appgestion.productos.Repository;

import com.rojas.spring.appgestion.productos.Model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

}

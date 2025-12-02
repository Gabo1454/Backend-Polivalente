package com.tienda.gamer.repository;

import com.tienda.gamer.model.Order;
import com.tienda.gamer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Buscar órdenes del usuario sin ordenar
    List<Order> findByUser(User user);

    // Buscar órdenes del usuario ordenadas por fecha
    List<Order> findByUserOrderByCreatedAtDesc(User user);
}

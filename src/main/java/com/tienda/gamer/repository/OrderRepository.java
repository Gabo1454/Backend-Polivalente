package com.tienda.gamer.repository;

import com.tienda.gamer.model.Order;
import com.tienda.gamer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByCreatedAtDesc(User user);
}

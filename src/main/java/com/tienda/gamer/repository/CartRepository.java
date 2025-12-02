package com.tienda.gamer.repository;

import com.tienda.gamer.model.Cart;
import com.tienda.gamer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    // Obtener carrito por entidad User
    Optional<Cart> findByUser(User user);

    // Obtener carrito por el ID del usuario
    Optional<Cart> findByUserId(Long userId);
}

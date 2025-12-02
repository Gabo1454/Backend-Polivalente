package com.tienda.gamer.repository;

import com.tienda.gamer.model.Cart;
import com.tienda.gamer.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // Obtiene los items de un carrito
    List<CartItem> findByCart(Cart cart);

    @Transactional
    void deleteByCart(Cart cart);

    // Si alguna vez quieres buscar por producto:
    List<CartItem> findByProductId(Long productId);


}

package com.tienda.gamer.repository;

import com.tienda.gamer.model.Order;
import com.tienda.gamer.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    /**
     * Devuelve los items de una orden, útil si luego quieres consultar
     * los detalles por orden sin cargar toda la entidad Order.
     */
    List<OrderItem> findByOrder(Order order);

    /**
     * Si alguna vez quieres buscar todos los OrderItem para un producto
     * (p. ej. historial de ventas), puedes usar:
     */
    List<OrderItem> findByProductId(Long productId);
}

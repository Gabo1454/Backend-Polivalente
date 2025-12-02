package com.tienda.gamer.service;

import com.tienda.gamer.model.*;
import com.tienda.gamer.repository.CartItemRepository;
import com.tienda.gamer.repository.CartRepository;
import com.tienda.gamer.repository.OrderItemRepository;
import com.tienda.gamer.repository.OrderRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;

    public OrderService(CartRepository cartRepository,
                        OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartItemRepository = cartItemRepository;
    }

    // ==========================
    //  CREAR ORDEN DESDE CARRITO
    // ==========================
    public Order createOrderFromCart(User user) {

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("El usuario no tiene carrito"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("El carrito está vacío");
        }

        // Crear orden
        Order order = Order.builder()
                .user(user)
                .status("CREATED")
                .totalAmount(calculateTotal(cart))
                .createdAt(LocalDateTime.now())
                .build();

        orderRepository.save(order);

        // Copiar items del carrito → order_items
        for (CartItem cartItem : cart.getItems()) {

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(cartItem.getProduct())
                    .quantity(cartItem.getQuantity())
                    .priceAtPurchase(cartItem.getProduct().getPrice())
                    .build();

            orderItemRepository.save(orderItem);
        }

        // Vaciar el carrito después de comprar
        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();
        cartRepository.save(cart);

        return order;
    }

    private int calculateTotal(Cart cart) {

        return cart.getItems().stream()
                .mapToInt(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
    }

    // ==========================
    //  OBTENER ÓRDENES DEL USUARIO
    // ==========================
    public List<Order> getOrdersForUser(User user) {
        return orderRepository.findByUser(user);
    }
}

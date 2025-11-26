package com.tienda.gamer.service;

import com.tienda.gamer.model.*;
import com.tienda.gamer.repository.CartRepository;
import com.tienda.gamer.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;

    public OrderService(CartRepository cartRepository,
                        OrderRepository orderRepository) {
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public Order createOrderFromCart(User user) {
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Carrito vacío"));

        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Carrito sin productos");
        }

        Order order = new Order();
        order.setUser(user);
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus("CREATED");

        int total = 0;

        for (CartItem cartItem : cart.getItems()) {
            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(cartItem.getProduct());
            oi.setQuantity(cartItem.getQuantity());
            oi.setPriceAtPurchase(cartItem.getProduct().getPrice());

            total += cartItem.getQuantity() * cartItem.getProduct().getPrice();
            order.getItems().add(oi);
        }

        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);

        // vaciar carrito
        cart.getItems().clear();
        cartRepository.save(cart);

        return saved;
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersForUser(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }
}

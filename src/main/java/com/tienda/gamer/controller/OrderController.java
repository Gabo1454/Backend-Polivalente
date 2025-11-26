package com.tienda.gamer.controller;

import com.tienda.gamer.model.Order;
import com.tienda.gamer.model.User;
import com.tienda.gamer.repository.UserRepository;
import com.tienda.gamer.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Órdenes", description = "Gestión de órdenes de compra")
@SecurityRequirement(name = "BearerAuth")
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    public OrderController(OrderService orderService,
                           UserRepository userRepository) {
        this.orderService = orderService;
        this.userRepository = userRepository;
    }

    private User getCurrentUser(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("No hay usuario autenticado");
        }

        String username = principal.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
    }

    @PostMapping("/checkout")
    @Operation(summary = "Crear orden a partir del carrito actual")
    public ResponseEntity<Order> checkout(Principal principal) {

        User user = getCurrentUser(principal);

        Order order = orderService.createOrderFromCart(user);

        return ResponseEntity.ok(order);
    }

    @GetMapping("/me")
    @Operation(summary = "Listar órdenes del usuario autenticado")
    public ResponseEntity<List<Order>> myOrders(Principal principal) {

        User user = getCurrentUser(principal);

        return ResponseEntity.ok(
                orderService.getOrdersForUser(user)
        );
    }
}

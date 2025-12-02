package com.tienda.gamer.controller;

import com.tienda.gamer.dto.cart.AddToCartRequest;
import com.tienda.gamer.dto.cart.CartResponse;
import com.tienda.gamer.model.User; // Asegúrate de que tu modelo User implemente UserDetails
import com.tienda.gamer.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // Importación clave
import org.springframework.web.bind.annotation.*;

@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/cart")
@Tag(name = "Carrito", description = "Operaciones del carrito de compras")
public class CartController {

    private final CartService cartService;
    // Eliminado AuthUserProvider y su inyección

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    @Operation(summary = "Obtener carrito del usuario autenticado")
    public ResponseEntity<CartResponse> getCart(
            // Spring Security inyecta el objeto User autenticado
            @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(cartService.getCart(user));
    }

    @PostMapping("/add")
    @Operation(summary = "Agregar producto al carrito")
    public ResponseEntity<CartResponse> addToCart(
            @AuthenticationPrincipal User user,
            @RequestBody AddToCartRequest request) {

        return ResponseEntity.ok(cartService.addToCart(user, request));
    }

    @DeleteMapping("/remove/{productId}")
    @Operation(summary = "Eliminar producto del carrito")
    public ResponseEntity<CartResponse> removeFromCart(
            @AuthenticationPrincipal User user,
            @PathVariable Long productId) {

        return ResponseEntity.ok(cartService.removeFromCart(user, productId));
    }

    @DeleteMapping("/clear")
    @Operation(summary = "Vaciar carrito")
    public ResponseEntity<Void> clearCart(
            @AuthenticationPrincipal User user) {

        cartService.clearCart(user);
        return ResponseEntity.noContent().build();
    }
}
package com.tienda.gamer.controller;

import com.tienda.gamer.dto.cart.AddToCartRequest;
import com.tienda.gamer.dto.cart.CartResponse;
import com.tienda.gamer.model.User;
import com.tienda.gamer.service.CartService;
import com.tienda.gamer.util.AuthUserProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/cart")
@Tag(name = "Carrito", description = "Operaciones del carrito de compras")
public class CartController {

    private final CartService cartService;
    private final AuthUserProvider authUserProvider;

    public CartController(CartService cartService,
                          AuthUserProvider authUserProvider) {
        this.cartService = cartService;
        this.authUserProvider = authUserProvider;
    }

    @GetMapping
    @Operation(summary = "Obtener carrito del usuario autenticado")
    public ResponseEntity<CartResponse> getCart() {
        User user = authUserProvider.getCurrentUserOrThrow();
        return ResponseEntity.ok(cartService.getCart(user));
    }

    @PostMapping("/add")
    @Operation(summary = "Agregar producto al carrito")
    public ResponseEntity<CartResponse> addToCart(@RequestBody AddToCartRequest request) {
        User user = authUserProvider.getCurrentUserOrThrow();
        return ResponseEntity.ok(cartService.addToCart(user, request));
    }

    @DeleteMapping("/remove/{productId}")
    @Operation(summary = "Eliminar producto del carrito")
    public ResponseEntity<CartResponse> removeFromCart(@PathVariable Long productId) {
        User user = authUserProvider.getCurrentUserOrThrow();
        return ResponseEntity.ok(cartService.removeFromCart(user, productId));
    }

    @DeleteMapping("/clear")
    @Operation(summary = "Vaciar carrito")
    public ResponseEntity<Void> clearCart() {
        User user = authUserProvider.getCurrentUserOrThrow();
        cartService.clearCart(user);
        return ResponseEntity.noContent().build();
    }
}

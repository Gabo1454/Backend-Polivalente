package com.tienda.gamer.service;

import com.tienda.gamer.dto.cart.AddToCartRequest;
import com.tienda.gamer.dto.cart.CartItemResponse;
import com.tienda.gamer.dto.cart.CartResponse;
import com.tienda.gamer.model.*;
import com.tienda.gamer.repository.CartRepository;
import com.tienda.gamer.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository,
                       ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public CartResponse addToCart(User user, AddToCartRequest request) {
        // 1. Obtener o crear el carrito (Correcto para persistencia)
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setUser(user);
                    return cartRepository.save(c);
                });

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        // 2. Buscar si ya existe el ítem
        CartItem existing = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        if (existing == null) {
            // 3. Si no existe: Crear nuevo CartItem
            CartItem item = CartItem.builder()
                    // NO es necesario item.setCart(cart) aquí si usas el método helper
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();

            // 🔥 CORRECCIÓN CLAVE: Usar el método addCartItem de la entidad Cart.
            // Esto sincroniza la lista del carrito Y la referencia cart del CartItem.
            cart.addCartItem(item);
        } else {
            // 3.b Si existe: Actualizar cantidad (Correcto)
            existing.setQuantity(existing.getQuantity() + request.getQuantity());
        }

        // 4. Guardar el carrito. Gracias al CascadeType.ALL en Cart,
        // los CartItem nuevos se guardarán automáticamente.
        Cart saved = cartRepository.save(cart);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(User user) {
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setUser(user);
                    return c;
                });
        return mapToResponse(cart);
    }

    // Los demás métodos (removeFromCart, clearCart) no requerían cambios en su lógica.

    @Transactional
    public CartResponse removeFromCart(User user, Long productId) {
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Carrito vacío"));

        cart.getItems().removeIf(i -> i.getProduct().getId().equals(productId));

        Cart saved = cartRepository.save(cart);
        return mapToResponse(saved);
    }

    @Transactional
    public void clearCart(User user) {
        cartRepository.findByUser(user).ifPresent(cart -> {
            cart.getItems().clear();
            cartRepository.save(cart);
        });
    }

    private CartResponse mapToResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(i -> new CartItemResponse(
                        i.getProduct().getId(),
                        i.getProduct().getName(),
                        i.getProduct().getPrice(),
                        i.getQuantity(),
                        i.getProduct().getPrice() * i.getQuantity()
                ))
                .toList();

        int total = items.stream()
                .mapToInt(CartItemResponse::getLineTotal)
                .sum();

        return new CartResponse(items, total);
    }
}
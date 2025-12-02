package com.tienda.gamer.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Un usuario tiene un carrito
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // SOLUCIÓN: Cambiar a FetchType.EAGER para cargar los ítems inmediatamente.
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<CartItem> items = new ArrayList<>();

    // Método utilitario para agregar items asegurando la relación
    public void addCartItem(CartItem item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);
        item.setCart(this); // Sincroniza la referencia inversa
    }

    public void removeCartItem(CartItem item) {
        this.items.remove(item);
        item.setCart(null);
    }
}
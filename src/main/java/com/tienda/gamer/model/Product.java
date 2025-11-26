package com.tienda.gamer.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID interno numérico

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer price; // en CLP

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer stock;

    @Column(length = 2000)
    private String description;

    // la ruta de la imagen, igual que en tu front
    private String image;

    // si el producto está en oferta (como tu campo offer)
    @Column(nullable = false)
    private boolean offer = false;

    // categorías múltiples: ["Juegos"], ["Hardware", "Ofertas"], etc.
    @ElementCollection
    @CollectionTable(name = "product_categories", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "category")
    private List<String> categories = new ArrayList<>();
}

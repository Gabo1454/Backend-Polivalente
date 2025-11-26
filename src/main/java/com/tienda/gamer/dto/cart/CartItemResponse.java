package com.tienda.gamer.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CartItemResponse {
    private Long productId;
    private String name;
    private int price;
    private int quantity;
    private int lineTotal;
}

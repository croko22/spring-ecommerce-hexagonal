package com.example.ecommerce.order.application.port.out;

import com.example.ecommerce.cart.domain.model.Cart;

public interface CartPort {

    Cart getCartByUserId(Long userId);

    void clearCart(Long userId);
}

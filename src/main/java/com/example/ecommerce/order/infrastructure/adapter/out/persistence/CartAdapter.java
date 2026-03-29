package com.example.ecommerce.order.infrastructure.adapter.out.persistence;

import com.example.ecommerce.cart.application.port.out.CartRepositoryPort;
import com.example.ecommerce.cart.domain.model.Cart;
import com.example.ecommerce.order.application.port.out.CartPort;
import org.springframework.stereotype.Component;

@Component
public class CartAdapter implements CartPort {

    private final CartRepositoryPort cartRepositoryPort;

    public CartAdapter(CartRepositoryPort cartRepositoryPort) {
        this.cartRepositoryPort = cartRepositoryPort;
    }

    @Override
    public Cart getCartByUserId(Long userId) {
        return cartRepositoryPort.findByUserId(userId)
                .orElseGet(() -> new Cart(null, userId));
    }

    @Override
    public void clearCart(Long userId) {
        cartRepositoryPort.findByUserId(userId).ifPresent(cart -> {
            cart.clear();
            cartRepositoryPort.save(cart);
        });
    }
}

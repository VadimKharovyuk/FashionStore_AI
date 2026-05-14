package com.example.fashionstore_ai.mapper;

import com.example.fashionstore_ai.dto.cart.CartItemResponse;
import com.example.fashionstore_ai.dto.cart.CartResponse;
import com.example.fashionstore_ai.model.Cart;
import com.example.fashionstore_ai.model.CartItem;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class CartMapper {

    // ── CartItem ──────────────────────────────────────────────────

    public CartItemResponse toResponse(CartItem item) {
        if (item == null) return null;

        return new CartItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getBrand(),
                item.getProduct().getImageUrl(),
                item.getSize(),
                item.getQuantity(),
                item.getPriceAtAdd(),
                item.getSubtotal(),
                item.getCreatedAt()
        );
    }

    public List<CartItemResponse> toResponseList(List<CartItem> items) {
        if (items == null || items.isEmpty()) return Collections.emptyList();
        return items.stream()
                .map(this::toResponse)
                .toList();
    }

    // ── Cart ──────────────────────────────────────────────────────

    public CartResponse toResponse(Cart cart) {
        if (cart == null) return null;

        return new CartResponse(
                cart.getId(),
                cart.getSessionId(),
                toResponseList(cart.getItems()),
                cart.getTotalItems(),
                cart.getTotalPrice(),
                cart.isEmpty(),
                cart.getUpdatedAt()
        );
    }
}

package com.example.fashionstore_ai.service;

import com.example.fashionstore_ai.dto.cart.CartResponse;
import com.example.fashionstore_ai.enums.Size;

public interface CartService {

    // отримати кошик (або створити порожній якщо немає)
    CartResponse getCart(String sessionId);

    // додати товар — якщо вже є такий productId+size → збільшити quantity
    CartResponse addToCart(String sessionId, Long productId, Size size, int quantity);

    // видалити конкретний item з кошика
    CartResponse removeFromCart(String sessionId, Long cartItemId);

    // змінити кількість конкретного item
    CartResponse updateQuantity(String sessionId, Long cartItemId, int quantity);

    // очистити весь кошик (викликається після оформлення замовлення)
    void clearCart(String sessionId);

    // кількість унікальних позицій (для бейджа в хедері)
    int getItemCount(String sessionId);
}

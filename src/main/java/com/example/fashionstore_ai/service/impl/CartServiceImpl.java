package com.example.fashionstore_ai.service.impl;

import com.example.fashionstore_ai.dto.cart.CartResponse;
import com.example.fashionstore_ai.enums.Size;
import com.example.fashionstore_ai.mapper.CartMapper;
import com.example.fashionstore_ai.model.Cart;
import com.example.fashionstore_ai.model.CartItem;
import com.example.fashionstore_ai.model.Product;
import com.example.fashionstore_ai.model.ProductSize;
import com.example.fashionstore_ai.repository.CartItemRepository;
import com.example.fashionstore_ai.repository.CartRepository;
import com.example.fashionstore_ai.repository.ProductRepository;
import com.example.fashionstore_ai.repository.ProductSizeRepository;
import com.example.fashionstore_ai.service.CartService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductSizeRepository productSizeRepository;
    private final CartMapper cartMapper;
    private final EntityManager entityManager;

    // ── getCart ───────────────────────────────────────────────────

    @Override
    @Transactional
    public CartResponse getCart(String sessionId) {
        Cart cart = getOrCreateCart(sessionId);
        return cartMapper.toResponse(cart);
    }

    // ── addToCart ─────────────────────────────────────────────────

    @Override
    @Transactional
    public CartResponse addToCart(String sessionId, Long productId, Size size, int quantity) {
        // 1. Перевіряємо що товар існує
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Товар не знайдено: id=" + productId));

        // 2. Перевіряємо наявність розміру на складі
        ProductSize productSize = productSizeRepository
                .findByProductIdAndSize(productId, size)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Розмір " + size + " не знайдено для товару: " + product.getName()));

        if (productSize.getStockQuantity() < quantity) {
            throw new IllegalStateException(
                    "Недостатньо товару на складі. Доступно: "
                            + productSize.getStockQuantity() + " шт.");
        }

        // 3. Отримуємо або створюємо кошик
        Cart cart = getOrCreateCart(sessionId);

        // 4. Якщо такий товар+розмір вже є — збільшуємо quantity
        cartItemRepository
                .findByCartIdAndProductIdAndSize(cart.getId(), productId, size)
                .ifPresentOrElse(
                        existingItem -> {
                            int newQty = existingItem.getQuantity() + quantity;
                            if (productSize.getStockQuantity() < newQty) {
                                throw new IllegalStateException(
                                        "Недостатньо товару на складі. Доступно: "
                                                + productSize.getStockQuantity() + " шт.");
                            }
                            existingItem.setQuantity(newQty);
                            cartItemRepository.save(existingItem);
                            log.debug("CartService: збільшено quantity для productId={} size={} → {}",
                                    productId, size, newQty);
                        },
                        () -> {
                            CartItem newItem = CartItem.builder()
                                    .cart(cart)
                                    .product(product)
                                    .size(size)
                                    .quantity(quantity)
                                    .priceAtAdd(product.getPrice())
                                    .build();
                            cartItemRepository.save(newItem);
                            log.debug("CartService: додано новий item productId={} size={} qty={}",
                                    productId, size, quantity);
                        }
                );

        // 5. Повертаємо оновлений кошик
        return cartMapper.toResponse(
                cartRepository.findBySessionIdWithItems(sessionId).orElse(cart));
    }

    // ── removeFromCart ────────────────────────────────────────────
    @Override
    @Transactional
    public CartResponse removeFromCart(String sessionId, Long cartItemId) {
        log.info("CartService.removeFromCart: sessionId={} cartItemId={}", sessionId, cartItemId);

        Cart cart = getOrCreateCart(sessionId);

        // знаходимо item в колекції cart — не через окремий репозиторій
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "Позицію кошика не знайдено: id=" + cartItemId));

        // видаляємо з колекції батька — orphanRemoval = true зробить DELETE
        cart.getItems().remove(item);
        cartRepository.save(cart);
        cartRepository.flush();

        log.info("CartService: видалено cartItemId={}, залишилось items={}",
                cartItemId, cart.getItems().size());

        return cartMapper.toResponse(cart);
    }

    // ── updateQuantity ────────────────────────────────────────────

    @Override
    @Transactional
    public CartResponse updateQuantity(String sessionId, Long cartItemId, int quantity) {
        if (quantity <= 0) {
            return removeFromCart(sessionId, cartItemId);
        }

        Cart cart = getOrCreateCart(sessionId);

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Позицію кошика не знайдено: id=" + cartItemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new SecurityException("Немає доступу до цієї позиції кошика");
        }

        // перевіряємо наявність
        productSizeRepository
                .findByProductIdAndSize(item.getProduct().getId(), item.getSize())
                .ifPresent(ps -> {
                    if (ps.getStockQuantity() < quantity) {
                        throw new IllegalStateException(
                                "Недостатньо товару на складі. Доступно: "
                                        + ps.getStockQuantity() + " шт.");
                    }
                });

        item.setQuantity(quantity);
        cartItemRepository.save(item);

        return cartMapper.toResponse(
                cartRepository.findBySessionIdWithItems(sessionId).orElse(cart));
    }

    // ── clearCart ─────────────────────────────────────────────────

    @Override
    @Transactional
    public void clearCart(String sessionId) {
        cartRepository.findBySessionId(sessionId).ifPresent(cart -> {
            cartItemRepository.deleteByCartId(cart.getId());
            log.info("CartService: кошик очищено для sessionId={}", sessionId);
        });
    }

    // ── getItemCount ──────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public int getItemCount(String sessionId) {
        return cartRepository.findBySessionId(sessionId)
                .map(cart -> cart.getItems().stream()
                        .mapToInt(CartItem::getQuantity)
                        .sum())
                .orElse(0);
    }

    // ── Private helpers ───────────────────────────────────────────

    private Cart getOrCreateCart(String sessionId) {
        return cartRepository.findBySessionIdWithItems(sessionId)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .sessionId(sessionId)
                            .build();
                    Cart saved = cartRepository.save(newCart);
                    log.debug("CartService: створено новий кошик для sessionId={}", sessionId);
                    return saved;
                });
    }
}
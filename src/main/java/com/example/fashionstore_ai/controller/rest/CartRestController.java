package com.example.fashionstore_ai.controller.rest;

import com.example.fashionstore_ai.config.SessionResolver;

import com.example.fashionstore_ai.dto.cart.CartItemRequest;
import com.example.fashionstore_ai.dto.cart.CartResponse;
import com.example.fashionstore_ai.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
public class CartRestController {

    private final CartService cartService;
    private final SessionResolver sessionResolver;

    @PostMapping("/add")
    public ResponseEntity<CartResponse> add(
            @Valid @RequestBody CartItemRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        String sessionId = sessionResolver.resolve(httpRequest, httpResponse);
        log.info("CartRestController.add: sessionId={} productId={} size={}",
                sessionId, request.productId(), request.size());
        return ResponseEntity.ok(
                cartService.addToCart(sessionId, request.productId(),
                        request.size(), request.quantity()));
    }

    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<CartResponse> remove(
            @PathVariable Long cartItemId,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        String sessionId = sessionResolver.resolve(httpRequest, httpResponse);
        log.info("CartRestController.remove: sessionId={} cartItemId={}", sessionId, cartItemId);
        return ResponseEntity.ok(cartService.removeFromCart(sessionId, cartItemId));
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        String sessionId = sessionResolver.resolve(httpRequest, httpResponse);
        return ResponseEntity.ok(cartService.getCart(sessionId));
    }
}
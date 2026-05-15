package com.example.fashionstore_ai.controller.rest;

import com.example.fashionstore_ai.config.SessionResolver;
import com.example.fashionstore_ai.dto.product.ProductResponse;
import com.example.fashionstore_ai.enums.*;
import com.example.fashionstore_ai.service.ProductService;
import com.example.fashionstore_ai.util.PageResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductRestController {

    private final ProductService productService;
    private final SessionResolver sessionResolver;

    // ── GET /api/products — каталог з фільтрами і пагінацією ─────

    @GetMapping
    public ResponseEntity<PageResponse<ProductResponse>> getCatalog(
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) Gender gender,
            @RequestParam(required = false) Season season,
            @RequestParam(required = false) Color color,
            @RequestParam(required = false) Material material,
            @RequestParam(required = false) FitType fitType,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        log.debug("ProductRestController.getCatalog: category={} gender={} page={} size={}",
                category, gender, page, size);

        PageResponse<ProductResponse> response = productService.getCatalog(
                category, gender, season, color,
                material, fitType, maxPrice, search,
                page, size
        );

        return ResponseEntity.ok(response);
    }

    // ── GET /api/products/{id} — товар по id ─────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(
            @PathVariable Long id,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        log.debug("ProductRestController.getById: id={}", id);

        // записуємо перегляд для RecommendationAgent
        String sessionId = sessionResolver.resolve(request, response);
        productService.recordView(sessionId, id);

        return ResponseEntity.ok(productService.getById(id));
    }

    // ── GET /api/products/{id}/similar — схожі товари ────────────

    @GetMapping("/{id}/similar")
    public ResponseEntity<?> getSimilar(
            @PathVariable Long id,
            @RequestParam(defaultValue = "4") int limit
    ) {
        return ResponseEntity.ok(productService.getSimilar(id, limit));
    }
}

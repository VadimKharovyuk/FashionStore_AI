package com.example.fashionstore_ai.controller;

import com.example.fashionstore_ai.config.SessionResolver;
import com.example.fashionstore_ai.dto.product.ProductResponse;
import com.example.fashionstore_ai.enums.*;
import com.example.fashionstore_ai.service.ProductService;
import com.example.fashionstore_ai.util.PageResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
public class ProductViewController {

    private final ProductService productService;
    private final SessionResolver sessionResolver;

    // ── GET /products — каталог ───────────────────────────────────

    @GetMapping
    public String catalog(
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) Gender gender,
            @RequestParam(required = false) Season season,
            @RequestParam(required = false) Color color,
            @RequestParam(required = false) Material material,
            @RequestParam(required = false) FitType fitType,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model
    ) {
        PageResponse<ProductResponse> products = productService.getCatalog(
                category, gender, season, color,
                material, fitType, maxPrice, search,
                page, size
        );

        // товари і пагінація
        model.addAttribute("products",     products.getContent());
        model.addAttribute("currentPage",  products.getCurrentPage());
        model.addAttribute("totalPages",   products.getTotalPages());
        model.addAttribute("totalElements",products.getTotalElements());
        model.addAttribute("hasNext",      products.isHasNext());
        model.addAttribute("hasPrevious",  products.isHasPrevious());

        // поточні фільтри — щоб зберегти стан форми
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedGender",   gender);
        model.addAttribute("selectedSeason",   season);
        model.addAttribute("selectedColor",    color);
        model.addAttribute("selectedMaterial", material);
        model.addAttribute("selectedFitType",  fitType);
        model.addAttribute("selectedMaxPrice", maxPrice);
        model.addAttribute("search",           search);

        // enum списки для фільтрів
        model.addAttribute("categories", Category.values());
        model.addAttribute("genders",    Gender.values());
        model.addAttribute("seasons",    Season.values());
        model.addAttribute("colors",     Color.values());
        model.addAttribute("materials",  Material.values());
        model.addAttribute("fitTypes",   FitType.values());

        return "products/catalog";
    }

    // ── GET /products/{id} — картка товару ───────────────────────

    @GetMapping("/{id}")
    public String productDetail(
            @PathVariable Long id,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model
    ) {
        // записуємо перегляд для RecommendationAgent
        String sessionId = sessionResolver.resolve(request, response);
        productService.recordView(sessionId, id);

        ProductResponse product = productService.getById(id);
        model.addAttribute("product", product);
        model.addAttribute("similar", productService.getSimilar(id, 4));

        return "products/detail";
    }
}

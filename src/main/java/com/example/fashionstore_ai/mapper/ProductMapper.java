package com.example.fashionstore_ai.mapper;
import com.example.fashionstore_ai.dto.product.ProductResponse;
import com.example.fashionstore_ai.enums.Size;
import com.example.fashionstore_ai.model.Product;
import com.example.fashionstore_ai.model.ProductSize;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product product) {
        if (product == null) return null;

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getBrand(),
                product.getSku(),
                product.getPrice(),
                calcDiscountedPrice(product),
                product.getDiscountPercent(),
                product.getCategory(),
                product.getGender(),
                product.getSeason(),
                product.getColor(),
                product.getColorDescription(),
                product.getMaterial(),
                product.getFabricComposition(),
                product.getFitType(),
                product.getCareInstructions(),
                product.getCountryOfOrigin(),
                product.getIsNew(),
                product.getIsBestseller(),
                product.getStyleNotes(),
                product.getImageUrl(),
                product.getTags(),
                buildAvailableSizes(product.getSizes())
        );
    }

    public List<ProductResponse> toResponseList(List<Product> products) {
        if (products == null || products.isEmpty()) return Collections.emptyList();
        return products.stream()
                .map(this::toResponse)
                .toList();
    }

    // ── Private helpers ───────────────────────────────────────────

    private BigDecimal calcDiscountedPrice(Product product) {
        if (product.getDiscountPercent() == null || product.getDiscountPercent() == 0) {
            return product.getPrice();
        }
        BigDecimal discount = BigDecimal.valueOf(product.getDiscountPercent())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return product.getPrice()
                .multiply(BigDecimal.ONE.subtract(discount))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private Map<Size, Integer> buildAvailableSizes(List<ProductSize> sizes) {
        if (sizes == null || sizes.isEmpty()) return Collections.emptyMap();
        return sizes.stream()
                .filter(ps -> ps.getStockQuantity() > 0)
                .collect(Collectors.toMap(
                        ProductSize::getSize,
                        ProductSize::getStockQuantity
                ));
    }
}

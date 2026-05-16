package com.example.fashionstore_ai.tools;
import com.example.fashionstore_ai.config.BaseTool;
import com.example.fashionstore_ai.dto.cart.CartResponse;
import com.example.fashionstore_ai.enums.Size;
import com.example.fashionstore_ai.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CartTool extends BaseTool {

    private final CartService cartService;

    @Tool(name = "getCart",
            description = """
                  Отримати поточний вміст кошика користувача.
                  Використовуй коли: користувач питає що в кошику,
                  хоче знати загальну суму, кількість товарів.
                  ВАЖЛИВО: передавай точний sessionId з системного промпту.
                  """)
    public String getCart(
            @ToolParam(description = "ID сесії користувача — береться з системного промпту")
            String sessionId
    ) {
        sessionId = normalizeSessionId(sessionId);
        log.info("Tool getCart: sessionId={}", sessionId);
        if (sessionId.isBlank()) return "Помилка: sessionId порожній.";

        CartResponse cart = cartService.getCart(sessionId);
        return formatCart(cart);
    }

    @Tool(name = "addToCart",
            description = """
              Додати товар у кошик користувача.
              Використовуй коли: користувач хоче купити товар,
              каже "додай", "хочу це", "беру це", "додати у кошик".
              ОБОВ'ЯЗКОВО викликай цей tool — без виклику товар НЕ буде доданий.
              Товар вважається доданим ТІЛЬКИ після успішного виклику цього tool.
              """)
    public String addToCart(
            ToolContext toolContext,

            @ToolParam(description = "ID товару")
            Long productId,

            @ToolParam(description = "Розмір: XS, S, M, L, XL, XXL")
            Size size,

            @ToolParam(description = "Кількість (зазвичай 1)")
            int quantity
    ) {
        String sessionId = (String) toolContext.getContext().get("sessionId");
        log.info("Tool addToCart: sessionId={} productId={} size={} qty={}",
                sessionId, productId, size, quantity);

        if (sessionId == null || sessionId.isBlank())
            return "Помилка: sessionId порожній.";

        try {
            CartResponse cart = cartService.addToCart(sessionId, productId, size, quantity);
            return "✅ Товар додано в кошик!\n\n" + formatCart(cart);
        } catch (IllegalStateException e) {
            return "❌ " + e.getMessage();
        } catch (Exception e) {
            return "❌ Не вдалось додати товар: " + e.getMessage();
        }
    }

    @Tool(name = "removeFromCart",
            description = """
                  Видалити позицію з кошика.
                  Використовуй коли: користувач хоче прибрати товар з кошика.
                  Потрібен cartItemId (не productId!) — його можна отримати через getCart.
                  ВАЖЛИВО: передавай точний sessionId з системного промпту.
                  """)
    public String removeFromCart(
            @ToolParam(description = "ID сесії користувача — береться з системного промпту")
            String sessionId,

            @ToolParam(description = "ID позиції кошика (cartItemId з getCart)")
            Long cartItemId
    ) {
        sessionId = normalizeSessionId(sessionId);
        log.info("Tool removeFromCart: sessionId={} cartItemId={}", sessionId, cartItemId);
        if (sessionId.isBlank()) return "Помилка: sessionId порожній.";

        try {
            CartResponse cart = cartService.removeFromCart(sessionId, cartItemId);
            return "✅ Товар видалено з кошика.\n\n" + formatCart(cart);
        } catch (Exception e) {
            return "❌ Не вдалось видалити товар: " + e.getMessage();
        }
    }

    @Tool(name = "updateCartQuantity",
            description = """
                  Змінити кількість товару в кошику.
                  Використовуй коли: користувач хоче більше або менше одиниць.
                  Якщо quantity=0 — товар буде видалено з кошика.
                  ВАЖЛИВО: передавай точний sessionId з системного промпту.
                  """)
    public String updateCartQuantity(
            @ToolParam(description = "ID сесії користувача — береться з системного промпту")
            String sessionId,

            @ToolParam(description = "ID позиції кошика (cartItemId)")
            Long cartItemId,

            @ToolParam(description = "Нова кількість (0 = видалити)")
            int quantity
    ) {
        sessionId = normalizeSessionId(sessionId);
        log.info("Tool updateCartQuantity: sessionId={} cartItemId={} qty={}",
                sessionId, cartItemId, quantity);
        if (sessionId.isBlank()) return "Помилка: sessionId порожній.";

        try {
            CartResponse cart = cartService.updateQuantity(sessionId, cartItemId, quantity);
            return "✅ Кількість оновлено.\n\n" + formatCart(cart);
        } catch (Exception e) {
            return "❌ Не вдалось оновити кількість: " + e.getMessage();
        }
    }

    // ── Format helper ─────────────────────────────────────────────

    private String formatCart(CartResponse cart) {
        if (cart.isEmpty()) {
            return "Кошик порожній.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Кошик (").append(cart.totalItems()).append(" од.):\n");

        cart.items().forEach(item -> {
            sb.append("• [id:").append(item.id()).append("] ")
                    .append(item.productName())
                    .append(" | ").append(item.brand())
                    .append(" | розмір ").append(item.size())
                    .append(" | ").append(item.quantity()).append(" шт.")
                    .append(" | $").append(item.subtotal())
                    .append("\n");
        });

        sb.append("\nЗагальна сума: $").append(cart.totalPrice());
        return sb.toString();
    }
}
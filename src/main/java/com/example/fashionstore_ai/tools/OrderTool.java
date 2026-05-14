package com.example.fashionstore_ai.tools;

import com.example.fashionstore_ai.dto.order.OrderResponse;
import com.example.fashionstore_ai.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderTool {

    private final OrderService orderService;

    @Tool(name = "getOrderByNumber",
            description = """
                  Отримати інформацію про замовлення по його номеру.
                  Використовуй коли: користувач питає про статус замовлення,
                  хоче знати деталі або що з його замовленням.
                  Номер замовлення має формат ORD-2026-001234.
                  """)
    public String getOrderByNumber(
            @ToolParam(description = "Номер замовлення у форматі ORD-2026-XXXXXX")
            String orderNumber,

            @ToolParam(description = "ID сесії користувача")
            String sessionId
    ) {
        log.info("Tool getOrderByNumber: orderNumber={} sessionId={}", orderNumber, sessionId);
        try {
            OrderResponse order = orderService.getByOrderNumber(orderNumber, sessionId);
            return formatOrder(order);
        } catch (Exception e) {
            return "Замовлення " + orderNumber + " не знайдено. " +
                    "Перевірте номер або sessionId.";
        }
    }

    @Tool(name = "getMyOrders",
            description = """
                  Отримати список всіх замовлень користувача.
                  Використовуй коли: користувач питає "мої замовлення",
                  "що я замовляв", "покажи мої покупки".
                  """)
    public String getMyOrders(
            @ToolParam(description = "ID сесії користувача")
            String sessionId
    ) {
        log.info("Tool getMyOrders: sessionId={}", sessionId);
        List<OrderResponse> orders = orderService.getAllBySession(sessionId);

        if (orders.isEmpty()) {
            return "Замовлень не знайдено для цієї сесії.";
        }

        StringBuilder sb = new StringBuilder("Ваші замовлення (" + orders.size() + "):\n\n");
        orders.forEach(o -> {
            sb.append("📦 ").append(o.orderNumber())
                    .append(" | ").append(o.status())
                    .append(" | $").append(o.totalPrice())
                    .append(" | ").append(o.createdAt().toLocalDate())
                    .append("\n");
        });
        return sb.toString();
    }

    @Tool(name = "trackShipment",
            description = """
                  Отримати інформацію про доставку і трекінг замовлення.
                  Використовуй коли: користувач питає де посилка,
                  який статус доставки, коли очікувати замовлення.
                  """)
    public String trackShipment(
            @ToolParam(description = "Номер замовлення")
            String orderNumber,

            @ToolParam(description = "ID сесії користувача")
            String sessionId
    ) {
        log.info("Tool trackShipment: orderNumber={} sessionId={}", orderNumber, sessionId);
        try {
            return orderService.getTrackingInfo(orderNumber, sessionId);
        } catch (Exception e) {
            return "Не вдалось отримати інформацію про доставку: " + e.getMessage();
        }
    }

    @Tool(name = "cancelOrder",
            description = """
                  Скасувати замовлення.
                  Використовуй коли: користувач хоче скасувати замовлення.
                  ВАЖЛИВО: завжди запитуй підтвердження перед скасуванням!
                  Скасування можливе тільки для статусів PENDING і CONFIRMED.
                  """)
    public String cancelOrder(
            @ToolParam(description = "Номер замовлення")
            String orderNumber,

            @ToolParam(description = "ID сесії користувача")
            String sessionId,

            @ToolParam(description = "Підтвердження скасування: true якщо користувач підтвердив")
            boolean confirmed
    ) {
        log.info("Tool cancelOrder: orderNumber={} confirmed={}", orderNumber, confirmed);

        if (!confirmed) {
            return "Для скасування замовлення " + orderNumber +
                    " потрібне підтвердження. Уточни у користувача: " +
                    "\"Ви впевнені що хочете скасувати замовлення " + orderNumber + "?\"";
        }

        try {
            OrderResponse order = orderService.cancelOrder(orderNumber, sessionId);
            return "✅ Замовлення " + orderNumber + " успішно скасовано. " +
                    "Статус: " + order.status();
        } catch (IllegalStateException e) {
            return "❌ " + e.getMessage();
        } catch (Exception e) {
            return "❌ Не вдалось скасувати замовлення: " + e.getMessage();
        }
    }

    @Tool(name = "updateDeliveryAddress",
            description = """
                  Змінити адресу доставки замовлення.
                  Використовуй коли: користувач хоче змінити адресу.
                  Зміна можлива тільки для PENDING і CONFIRMED замовлень.
                  Завжди підтверджуй нову адресу з користувачем.
                  """)
    public String updateDeliveryAddress(
            @ToolParam(description = "Номер замовлення")
            String orderNumber,

            @ToolParam(description = "ID сесії користувача")
            String sessionId,

            @ToolParam(description = "Нова адреса доставки")
            String newAddress,

            @ToolParam(description = "Підтвердження: true якщо користувач підтвердив нову адресу")
            boolean confirmed
    ) {
        log.info("Tool updateDeliveryAddress: orderNumber={} confirmed={}", orderNumber, confirmed);

        if (!confirmed) {
            return "Уточни у користувача: \"Підтверджуєте зміну адреси на: " + newAddress + "?\"";
        }

        try {
            orderService.updateDeliveryAddress(orderNumber, sessionId, newAddress);
            return "✅ Адресу доставки змінено на: " + newAddress;
        } catch (Exception e) {
            return "❌ " + e.getMessage();
        }
    }

    @Tool(name = "createReturnRequest",
            description = """
                  Створити запит на повернення товару.
                  Використовуй коли: користувач хоче повернути товар.
                  Повернення можливе тільки для DELIVERED замовлень.
                  Потрібен itemId — його можна отримати через getOrderByNumber.
                  """)
    public String createReturnRequest(
            @ToolParam(description = "Номер замовлення")
            String orderNumber,

            @ToolParam(description = "ID сесії користувача")
            String sessionId,

            @ToolParam(description = "ID позиції замовлення (itemId)")
            Long itemId,

            @ToolParam(description = "Причина повернення")
            String reason,

            @ToolParam(description = "Підтвердження: true якщо користувач підтвердив")
            boolean confirmed
    ) {
        log.info("Tool createReturnRequest: orderNumber={} itemId={} confirmed={}",
                orderNumber, itemId, confirmed);

        if (!confirmed) {
            return "Уточни у користувача: \"Підтверджуєте запит на повернення? Причина: " +
                    reason + "\"";
        }

        try {
            orderService.createReturnRequest(orderNumber, sessionId, itemId, reason);
            return "✅ Запит на повернення створено. " +
                    "Наш менеджер зв'яжеться з вами протягом 1-2 робочих днів.";
        } catch (Exception e) {
            return "❌ " + e.getMessage();
        }
    }

    @Tool(name = "getReturnPolicy",
            description = """
                  Отримати умови повернення товарів.
                  Використовуй коли: користувач питає про політику повернень,
                  умови обміну, терміни повернення.
                  """)
    public String getReturnPolicy() {
        return """
                📋 Політика повернень FashionStore:
                
                • Термін повернення: 14 днів з моменту отримання
                • Умови: товар не носили, зберіг вигляд і бирки
                • Повернення коштів: 3-5 робочих днів після отримання
                • Обмін: безкоштовно при наявності потрібного розміру
                • Доставка при поверненні: за рахунок покупця
                
                Для оформлення повернення — скажіть номер замовлення і причину.
                """;
    }

    // ── Format helper ─────────────────────────────────────────────

    private String formatOrder(OrderResponse o) {
        StringBuilder sb = new StringBuilder();
        sb.append("📦 Замовлення: ").append(o.orderNumber()).append("\n");
        sb.append("Статус: ").append(o.status()).append("\n");
        sb.append("Сума: $").append(o.totalPrice()).append("\n");
        sb.append("Отримувач: ").append(o.recipientName()).append("\n");
        sb.append("Адреса: ").append(o.deliveryAddress()).append("\n");

        if (o.trackingNumber() != null) {
            sb.append("Трекінг: ").append(o.trackingNumber()).append("\n");
        }
        if (o.estimatedDelivery() != null) {
            sb.append("Очікувана доставка: ").append(o.estimatedDelivery()).append("\n");
        }

        sb.append("\nТовари:\n");
        o.items().forEach(item ->
                sb.append("• ").append(item.productName())
                        .append(" | ").append(item.size())
                        .append(" | ").append(item.quantity()).append("шт")
                        .append(" | $").append(item.subtotal())
                        .append(item.returnStatus() != null ? " [" + item.returnStatus() + "]" : "")
                        .append("\n")
        );

        sb.append("\nДоступні дії: ");
        if (o.isCancellable())       sb.append("скасувати | ");
        if (o.isAddressChangeable()) sb.append("змінити адресу | ");
        if (o.isReturnable())        sb.append("повернення | ");

        return sb.toString();
    }
}

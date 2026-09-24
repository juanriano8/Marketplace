package com.marketplace.api.modules.order.application.dto;

import com.marketplace.api.modules.order.domain.model.OrderStatus;
import com.marketplace.api.modules.order.domain.model.SubOrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Order representation returned to buyers, sellers and administrators")
public record OrderResponse(
    UUID orderId,
    String orderNumber,
    UUID buyerId,
    BigDecimal totalAmount,
    String currencyCode,
    OrderStatus status,
    String paymentReference,
    Instant paidAt,
    Instant createdAt,
    List<SubOrderResponse> subOrders
) {

    @Schema(description = "Seller-scoped portion of an order, including dispatch data")
    public record SubOrderResponse(
        UUID subOrderId,
        UUID sellerId,
        BigDecimal subtotal,
        String currencyCode,
        SubOrderStatus status,
        String trackingNumber,
        String carrier,
        Instant shippedAt,
        Instant deliveredAt,
        List<OrderItemResponse> items
    ) {}

    @Schema(description = "Immutable purchased product line")
    public record OrderItemResponse(
        UUID orderItemId,
        UUID productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal,
        String currencyCode
    ) {}
}

package com.marketplace.api.modules.order.application.dto;

import com.marketplace.api.modules.order.domain.model.CartStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Buyer's active cart with its lines and computed subtotal")
public record CartResponse(
    UUID cartId,
    UUID buyerId,
    CartStatus status,
    List<CartItemResponse> items,
    int totalItemCount,
    BigDecimal subtotal,
    String currencyCode,
    Instant updatedAt
) {

    @Schema(description = "A single cart line")
    public record CartItemResponse(
        UUID itemId,
        UUID productId,
        UUID sellerId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal,
        String currencyCode
    ) {}
}

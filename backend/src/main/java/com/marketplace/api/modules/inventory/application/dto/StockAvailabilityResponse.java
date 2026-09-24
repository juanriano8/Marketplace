package com.marketplace.api.modules.inventory.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Stock availability of a product as seen by the inventory module")
public record StockAvailabilityResponse(
    @Schema(description = "Product identifier")
    UUID productId,
    @Schema(description = "Seller that owns the stock")
    UUID sellerId,
    @Schema(description = "Units physically on hand", example = "120")
    int availableQuantity,
    @Schema(description = "Units reserved by paid orders not yet dispatched", example = "5")
    int reservedQuantity,
    @Schema(description = "Units a buyer may add to a cart right now", example = "115")
    int sellableQuantity,
    @Schema(description = "Low-stock threshold configured for the product", example = "10")
    int lowStockThreshold,
    @Schema(description = "true when sellable quantity is at or below the threshold", example = "false")
    boolean lowStock,
    @Schema(description = "true when a buyer can currently purchase the product", example = "true")
    boolean available,
    @Schema(description = "Last time stock was adjusted")
    Instant lastAdjustedAt
) {}

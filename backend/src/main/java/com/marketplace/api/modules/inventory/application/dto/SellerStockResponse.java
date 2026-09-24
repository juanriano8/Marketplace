package com.marketplace.api.modules.inventory.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Stock snapshot as seen by the owning seller")
public record SellerStockResponse(
    UUID stockItemId,
    UUID productId,
    UUID sellerId,
    int availableQuantity,
    int reservedQuantity,
    int sellableQuantity,
    int lowStockThreshold,
    boolean lowStock,
    Instant lastAdjustedAt
) {}

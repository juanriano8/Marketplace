package com.marketplace.api.modules.inventory.application.dto;

import com.marketplace.api.modules.inventory.domain.model.StockMovementType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Audit entry describing one stock mutation")
public record StockMovementResponse(
    UUID movementId,
    UUID productId,
    UUID sellerId,
    StockMovementType movementType,
    int quantityDelta,
    int quantityBefore,
    int quantityAfter,
    String reason,
    UUID performedBy,
    String reference,
    Instant occurredAt
) {}

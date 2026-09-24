package com.marketplace.api.modules.inventory.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Seller request to correct physical stock.
 *
 * <p>Either {@code quantityDelta} (relative correction) or {@code physicalQuantity} (absolute
 * recount) must be supplied — never both — which is validated in the service layer where the
 * current stock level is known.</p>
 */
@Schema(description = "Request body for a seller physical stock adjustment")
public record StockAdjustmentRequest(
    @Schema(description = "Product whose stock is being adjusted", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @NotNull(message = "Product id is required")
    UUID productId,

    @Schema(description = "Signed delta applied to available stock, e.g. -3 or 25", example = "-3")
    Integer quantityDelta,

    @Schema(description = "Absolute physical count, replaces the current available stock", example = "40")
    Integer physicalQuantity,

    @Schema(description = "Justification recorded in the audit trail", example = "Damaged units removed after inventory count")
    String reason
) {

    public boolean hasDelta() {
        return quantityDelta != null;
    }

    public boolean hasPhysicalQuantity() {
        return physicalQuantity != null;
    }
}

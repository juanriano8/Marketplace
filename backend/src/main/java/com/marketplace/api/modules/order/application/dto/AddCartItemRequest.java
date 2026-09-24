package com.marketplace.api.modules.order.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Request body to add a product to the active cart")
public record AddCartItemRequest(
    @Schema(description = "Product identifier", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @NotNull(message = "Product id is required")
    UUID productId,

    @Schema(description = "Number of units to add", example = "2")
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 999, message = "Quantity cannot exceed 999 units per item")
    Integer quantity
) {}

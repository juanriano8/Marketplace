package com.marketplace.api.modules.order.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request body to set the absolute quantity of a cart line (0 removes the line)")
public record UpdateCartItemRequest(
    @Schema(description = "New quantity for the line", example = "3")
    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    Integer quantity
) {}

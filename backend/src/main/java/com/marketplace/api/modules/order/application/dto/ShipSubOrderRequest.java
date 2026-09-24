package com.marketplace.api.modules.order.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for a seller to register the dispatch of a sub-order")
public record ShipSubOrderRequest(
    @Schema(description = "Carrier tracking number", example = "TRK-9938472635")
    @NotBlank(message = "Tracking number is required")
    @Size(max = 120, message = "Tracking number must not exceed 120 characters")
    String trackingNumber,

    @Schema(description = "Carrier or logistics operator", example = "DHL")
    @Size(max = 120, message = "Carrier must not exceed 120 characters")
    String carrier
) {}

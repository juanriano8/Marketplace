package com.marketplace.api.modules.order.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for a buyer checkout")
public record CheckoutRequest(
    @Schema(
        description = "Optional buyer note forwarded to the payment provider / order record",
        example = "Please deliver after 6pm"
    )
    String notes
) {}

package com.marketplace.api.modules.order.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Result of a successful checkout: the created order plus the payment outcome")
public record CheckoutResponse(
    OrderResponse order,
    @Schema(description = "true once the payment gateway captured the funds", example = "true")
    boolean paymentCaptured,
    @Schema(description = "Gateway payment reference", example = "stub-9f1c...")
    String paymentReference,
    @Schema(description = "Total amount charged", example = "149.98")
    BigDecimal chargedAmount,
    @Schema(description = "Optional redirect URL when the provider requires buyer confirmation")
    String redirectUrl,
    @Schema(description = "Human readable payment detail", example = "Captured by stub gateway")
    String paymentMessage
) {}

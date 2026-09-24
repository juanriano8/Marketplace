package com.marketplace.api.modules.product.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Admin request to approve or reject a seller account")
public record SellerVerificationRequest(
    @Schema(description = "true to approve the seller, false to reject", example = "true")
    @NotNull(message = "Approved flag is required")
    Boolean approved
) {}

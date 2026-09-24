package com.marketplace.api.modules.product.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request body for admin approval/rejection of a product")
public record ProductApprovalRequest(
    @Schema(description = "true to approve, false to reject", example = "true")
    @NotNull(message = "Approval decision is required")
    Boolean approved
) {}

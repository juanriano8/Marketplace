package com.marketplace.api.modules.review.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Request body for a buyer to review a purchased product")
public record CreateReviewRequest(
    @Schema(description = "Product being reviewed", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @NotNull(message = "Product id is required")
    UUID productId,

    @Schema(description = "Rating from 1 to 5", example = "5")
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    Integer rating,

    @Schema(description = "Short headline", example = "Excellent sound quality")
    @Size(max = 150, message = "Title must not exceed 150 characters")
    String title,

    @Schema(description = "Detailed opinion", example = "Battery lasts all week and the noise cancelling works great.")
    @Size(max = 2000, message = "Comment must not exceed 2000 characters")
    String comment
) {}

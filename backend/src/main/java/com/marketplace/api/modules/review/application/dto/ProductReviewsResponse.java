package com.marketplace.api.modules.review.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

@Schema(description = "Paginated product reviews together with the aggregate rating")
public record ProductReviewsResponse(
    @Schema(description = "Product the reviews belong to")
    java.util.UUID productId,
    @Schema(description = "Average rating of visible reviews, null when there are none", example = "4.5")
    Double averageRating,
    @Schema(description = "Number of visible reviews", example = "12")
    long totalReviews,
    @Schema(description = "Page of reviews")
    Page<ReviewResponse> reviews
) {}

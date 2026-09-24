package com.marketplace.api.modules.review.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "A product review")
public record ReviewResponse(
    UUID reviewId,
    UUID productId,
    UUID sellerId,
    UUID buyerId,
    int rating,
    String title,
    String comment,
    boolean verifiedPurchase,
    boolean visible,
    Instant createdAt
) {}

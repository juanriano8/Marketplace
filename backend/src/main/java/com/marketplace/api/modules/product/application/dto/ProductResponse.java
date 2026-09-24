package com.marketplace.api.modules.product.application.dto;

import com.marketplace.api.modules.product.domain.model.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Product data returned in responses")
public record ProductResponse(
    UUID id,
    String name,
    String description,
    String slug,
    BigDecimal price,
    String currencyCode,
    Integer stockQuantity,
    String imageUrl,
    ProductStatus status,
    UUID sellerId,
    String category,
    Instant createdAt,
    Instant updatedAt
) {}

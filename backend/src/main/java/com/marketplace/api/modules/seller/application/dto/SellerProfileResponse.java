package com.marketplace.api.modules.seller.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Perfil público de un vendedor")
public record SellerProfileResponse(
    UUID sellerId,
    String storeName,
    String description,
    String logoUrl,
    BigDecimal ratingAvg,
    long ratingCount
) {}

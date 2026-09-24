package com.marketplace.api.modules.product.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Request body to update an existing product")
public record UpdateProductRequest(
    @Schema(description = "Product name", example = "Wireless Headphones Pro")
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    String name,

    @Schema(description = "Product description")
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    String description,

    @Schema(description = "Price in USD", example = "59.99")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    BigDecimal price,

    @Schema(description = "Available stock quantity", example = "50")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    Integer stockQuantity,

    @Schema(description = "Product category", example = "Electronics")
    String category,

    @Schema(description = "Product image URL")
    String imageUrl
) {}

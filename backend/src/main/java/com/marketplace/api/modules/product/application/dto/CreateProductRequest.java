package com.marketplace.api.modules.product.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Request body to create a new product")
public record CreateProductRequest(
    @Schema(description = "Product name", example = "Wireless Headphones")
    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    String name,

    @Schema(description = "Product description", example = "High quality wireless headphones with noise cancellation")
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    String description,

    @Schema(description = "Price in USD", example = "49.99")
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    BigDecimal price,

    @Schema(description = "Currency code ISO 4217", example = "USD")
    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
    String currencyCode,

    @Schema(description = "Available stock quantity", example = "100")
    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    Integer stockQuantity,

    @Schema(description = "Product category", example = "Electronics")
    @NotBlank(message = "Category is required")
    String category,

    @Schema(description = "Product image URL", example = "https://example.com/image.jpg")
    String imageUrl
) {}

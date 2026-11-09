package com.marketplace.api.modules.seller.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos editables del perfil del vendedor")
public record UpdateSellerProfileRequest(
    @Schema(description = "Nombre de la tienda", example = "Tienda de Diego")
    @Size(max = 160, message = "Store name must not exceed 160 characters")
    String storeName,

    @Schema(description = "Descripción de la tienda")
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    String description,

    @Schema(description = "Logo de la tienda")
    @Size(max = 255, message = "Logo URL must not exceed 255 characters")
    @Pattern(
        regexp = "^(https?://\\S+|/(?!/)\\S*)?$",
        message = "Logo URL must be an http(s) URL or a relative path"
    )
    String logoUrl
) {}

package com.marketplace.api.modules.auth.application.dto;

import com.marketplace.api.shared.security.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Authentication response containing JWT token and basic user info")
public record AuthResponse(
    @Schema(description = "JWT Access Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    String accessToken,

    @Schema(description = "Token type", example = "Bearer")
    String tokenType,

    @Schema(description = "Token expiration in milliseconds", example = "86400000")
    Long expiresIn,

    @Schema(description = "User unique identifier")
    UUID userId,

    @Schema(description = "User email address", example = "user@example.com")
    String email,

    @Schema(description = "User role", example = "ROLE_BUYER")
    UserRole role,

    @Schema(description = "Whether seller account is approved (only applicable for ROLE_SELLER)", example = "false")
    Boolean sellerApproved
) {
    public static AuthResponse of(
        String accessToken,
        Long expiresIn,
        UUID userId,
        String email,
        UserRole role,
        Boolean sellerApproved
    ) {
        return new AuthResponse(
            accessToken,
            "Bearer",
            expiresIn,
            userId,
            email,
            role,
            sellerApproved
        );
    }
}

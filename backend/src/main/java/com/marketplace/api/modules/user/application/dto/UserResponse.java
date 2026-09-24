package com.marketplace.api.modules.user.application.dto;

import com.marketplace.api.shared.security.UserRole;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String email,
    UserRole role,
    boolean enabled,
    Boolean sellerApproved,
    Instant createdAt
) {}

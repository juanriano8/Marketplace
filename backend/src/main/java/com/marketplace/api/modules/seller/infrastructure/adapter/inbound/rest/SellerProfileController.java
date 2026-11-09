package com.marketplace.api.modules.seller.infrastructure.adapter.inbound.rest;

import com.marketplace.api.modules.seller.application.dto.SellerProfileResponse;
import com.marketplace.api.modules.seller.application.dto.UpdateSellerProfileRequest;
import com.marketplace.api.modules.seller.domain.port.inbound.SellerProfileUseCase;
import com.marketplace.api.shared.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Sellers - Profile", description = "Perfil público y configuración del vendedor")
public class SellerProfileController {

    private final SellerProfileUseCase sellerProfileUseCase;

    @Operation(summary = "Perfil público de un vendedor")
    @ApiResponse(responseCode = "200", description = "Perfil encontrado")
    @GetMapping("/api/v1/sellers/{sellerId}/profile")
    public ResponseEntity<SellerProfileResponse> publicProfile(@PathVariable UUID sellerId) {
        return ResponseEntity.ok(sellerProfileUseCase.getPublicProfile(sellerId));
    }

    @Operation(summary = "Mi perfil de vendedor")
    @SecurityRequirement(name = "BearerAuth")
    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/api/v1/seller/profile")
    public ResponseEntity<SellerProfileResponse> myProfile(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(sellerProfileUseCase.getMyProfile(principal.id()));
    }

    @Operation(summary = "Actualizar mi perfil de vendedor")
    @SecurityRequirement(name = "BearerAuth")
    @PreAuthorize("hasRole('SELLER')")
    @PutMapping("/api/v1/seller/profile")
    public ResponseEntity<SellerProfileResponse> updateMyProfile(
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody UpdateSellerProfileRequest request
    ) {
        return ResponseEntity.ok(sellerProfileUseCase.updateMyProfile(principal.id(), request));
    }
}

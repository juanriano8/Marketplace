package com.marketplace.api.modules.admin.infrastructure.adapter.inbound.rest;

import com.marketplace.api.modules.admin.application.service.AdminService;
import com.marketplace.api.modules.product.application.dto.SellerVerificationRequest;
import com.marketplace.api.modules.user.application.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/sellers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Sellers", description = "Administrative seller moderation endpoints")
@SecurityRequirement(name = "BearerAuth")
public class AdminSellerController {

    private final AdminService adminService;

    @Operation(
        summary = "Verify a seller account",
        description = "Approves or rejects a pending seller registration. Rejecting a seller also disables the account."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Seller status updated"),
        @ApiResponse(responseCode = "400", description = "User is not a seller or payload is invalid"),
        @ApiResponse(responseCode = "403", description = "Requester is not an administrator"),
        @ApiResponse(responseCode = "404", description = "Seller not found")
    })
    @PatchMapping("/{sellerId}/verify")
    public ResponseEntity<UserResponse> verifySeller(
        @PathVariable UUID sellerId,
        @Valid @RequestBody SellerVerificationRequest request
    ) {
        return ResponseEntity.ok(adminService.verifySeller(sellerId, request));
    }
}

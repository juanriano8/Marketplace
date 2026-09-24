package com.marketplace.api.modules.inventory.infrastructure.adapter.inbound.rest;

import com.marketplace.api.modules.inventory.application.dto.SellerStockResponse;
import com.marketplace.api.modules.inventory.application.dto.StockAdjustmentRequest;
import com.marketplace.api.modules.inventory.domain.port.inbound.InventoryUseCase;
import com.marketplace.api.shared.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/seller/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SELLER')")
@Tag(name = "Inventory - Seller", description = "Seller physical stock management")
@SecurityRequirement(name = "BearerAuth")
public class SellerInventoryController {

    private final InventoryUseCase inventoryUseCase;

    @Operation(
        summary = "Adjust physical stock",
        description = "Applies a relative delta or an absolute physical recount to a product owned by the authenticated seller. Every change is written to the audit trail."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Stock adjusted"),
        @ApiResponse(responseCode = "400", description = "Invalid payload or adjustment would make stock negative"),
        @ApiResponse(responseCode = "403", description = "The product is not owned by the authenticated seller"),
        @ApiResponse(responseCode = "404", description = "No stock record for the product")
    })
    @PostMapping("/adjust")
    public ResponseEntity<SellerStockResponse> adjustStock(
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody StockAdjustmentRequest request
    ) {
        return ResponseEntity.ok(inventoryUseCase.adjustStock(principal.id(), request));
    }

    @Operation(summary = "List own stock", description = "Paginated stock levels for every product owned by the authenticated seller.")
    @ApiResponse(responseCode = "200", description = "Stock page returned")
    @GetMapping
    public ResponseEntity<Page<SellerStockResponse>> listOwnStock(
        @AuthenticationPrincipal UserPrincipal principal,
        @ParameterObject @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(inventoryUseCase.listSellerStock(principal.id(), pageable));
    }
}

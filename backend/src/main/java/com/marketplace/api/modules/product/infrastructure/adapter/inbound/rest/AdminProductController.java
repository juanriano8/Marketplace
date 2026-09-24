package com.marketplace.api.modules.product.infrastructure.adapter.inbound.rest;

import com.marketplace.api.modules.product.application.dto.ProductApprovalRequest;
import com.marketplace.api.modules.product.application.dto.ProductResponse;
import com.marketplace.api.modules.product.domain.model.ProductStatus;
import com.marketplace.api.modules.product.domain.port.inbound.ProductUseCase;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Products - Admin", description = "Product moderation")
@SecurityRequirement(name = "BearerAuth")
public class AdminProductController {

    private final ProductUseCase productUseCase;

    @Operation(
        summary = "List products for moderation",
        description = "Paginated catalog view for administrators, optionally filtered by status (defaults to PENDING_APPROVAL)."
    )
    @ApiResponse(responseCode = "200", description = "Product page returned")
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> listProductsByStatus(
        @RequestParam(required = false, defaultValue = "PENDING_APPROVAL") ProductStatus status,
        @ParameterObject @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(productUseCase.listProductsByStatus(status, pageable));
    }

    @Operation(
        summary = "Approve or reject a product",
        description = "Moderates a product in PENDING_APPROVAL: approved products become ACTIVE and visible in the public catalog."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Moderation decision applied"),
        @ApiResponse(responseCode = "400", description = "Product is not in a moderable state"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @PatchMapping("/{id}/approval")
    public ResponseEntity<ProductResponse> moderateProduct(
        @PathVariable UUID id,
        @Valid @RequestBody ProductApprovalRequest request
    ) {
        return ResponseEntity.ok(productUseCase.approveOrRejectProduct(id, request));
    }
}

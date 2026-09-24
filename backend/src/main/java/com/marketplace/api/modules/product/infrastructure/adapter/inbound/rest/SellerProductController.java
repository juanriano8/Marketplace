package com.marketplace.api.modules.product.infrastructure.adapter.inbound.rest;

import com.marketplace.api.modules.product.application.dto.CreateProductRequest;
import com.marketplace.api.modules.product.application.dto.ProductResponse;
import com.marketplace.api.modules.product.application.dto.UpdateProductRequest;
import com.marketplace.api.modules.product.domain.port.inbound.ProductUseCase;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/seller/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SELLER')")
@Tag(name = "Products - Seller", description = "Seller catalog management")
@SecurityRequirement(name = "BearerAuth")
public class SellerProductController {

    private final ProductUseCase productUseCase;

    @Operation(
        summary = "List own catalog",
        description = "Paginated catalog of the authenticated seller, including products that are still pending approval."
    )
    @ApiResponse(responseCode = "200", description = "Product page returned")
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> listOwnProducts(
        @AuthenticationPrincipal UserPrincipal principal,
        @ParameterObject @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(productUseCase.listSellerProducts(principal.id(), pageable));
    }

    @Operation(
        summary = "Create a product",
        description = "Creates a product owned by the authenticated seller. New products start in PENDING_APPROVAL and only verified sellers may publish."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Product created and awaiting moderation"),
        @ApiResponse(responseCode = "400", description = "Invalid payload or the seller account is not verified")
    })
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody CreateProductRequest request
    ) {
        ProductResponse response = productUseCase.createProduct(request, principal.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Update own product", description = "Updates a product; sellers can only modify products they own.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product updated"),
        @ApiResponse(responseCode = "400", description = "Invalid payload or product does not belong to the seller"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable UUID id,
        @Valid @RequestBody UpdateProductRequest request
    ) {
        return ResponseEntity.ok(productUseCase.updateProduct(id, request, principal.id()));
    }
}

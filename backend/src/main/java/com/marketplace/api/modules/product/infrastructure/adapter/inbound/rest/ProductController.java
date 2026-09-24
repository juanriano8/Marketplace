package com.marketplace.api.modules.product.infrastructure.adapter.inbound.rest;

import com.marketplace.api.modules.product.application.dto.ProductResponse;
import com.marketplace.api.modules.product.domain.port.inbound.ProductUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products - Public catalog", description = "Public read-only product catalog endpoints")
public class ProductController {

    private final ProductUseCase productUseCase;

    @Operation(summary = "List active products", description = "Paginated public listing of products that are ACTIVE.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Page of active products returned")
    })
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> listProducts(
        @ParameterObject @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(productUseCase.listActiveProducts(pageable));
    }

    @Operation(summary = "Get product detail by slug", description = "Returns a single ACTIVE product identified by its public slug.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product found"),
        @ApiResponse(responseCode = "404", description = "Product not found or not active")
    })
    @GetMapping("/{slug}")
    public ResponseEntity<ProductResponse> getProductBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(productUseCase.getProductBySlug(slug));
    }
}

package com.marketplace.api.modules.review.infrastructure.adapter.inbound.rest;

import com.marketplace.api.modules.review.application.dto.ProductReviewsResponse;
import com.marketplace.api.modules.review.domain.port.inbound.ReviewUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews - Public", description = "Public product opinions")
public class ProductReviewController {

    private final ReviewUseCase reviewUseCase;

    @Operation(
        summary = "List product reviews",
        description = "Paginated visible opinions for a product plus the aggregate average rating."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reviews returned"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/product/{productId}")
    public ResponseEntity<ProductReviewsResponse> listProductReviews(
        @PathVariable UUID productId,
        @ParameterObject @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(reviewUseCase.listProductReviews(productId, pageable));
    }
}

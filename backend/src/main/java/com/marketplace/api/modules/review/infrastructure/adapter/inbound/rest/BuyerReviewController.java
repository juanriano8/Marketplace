package com.marketplace.api.modules.review.infrastructure.adapter.inbound.rest;

import com.marketplace.api.modules.review.application.dto.CreateReviewRequest;
import com.marketplace.api.modules.review.application.dto.ReviewResponse;
import com.marketplace.api.modules.review.domain.port.inbound.ReviewUseCase;
import com.marketplace.api.shared.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/buyer/reviews")
@RequiredArgsConstructor
@PreAuthorize("hasRole('BUYER')")
@Tag(name = "Reviews - Buyer", description = "Verified purchase reviews")
@SecurityRequirement(name = "BearerAuth")
public class BuyerReviewController {

    private final ReviewUseCase reviewUseCase;

    @Operation(
        summary = "Create a review",
        description = "Creates a review for a product the authenticated buyer actually received (order in SHIPPED or DELIVERED state). One review per buyer and product."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Review created"),
        @ApiResponse(responseCode = "400", description = "Unverified purchase, duplicate review or invalid payload"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody CreateReviewRequest request
    ) {
        ReviewResponse response = reviewUseCase.createReview(principal.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

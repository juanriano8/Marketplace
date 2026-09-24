package com.marketplace.api.modules.review.infrastructure.adapter.inbound.rest;

import com.marketplace.api.modules.review.application.dto.ReviewResponse;
import com.marketplace.api.modules.review.domain.port.inbound.ReviewUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/reviews")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Reviews - Admin", description = "Review moderation")
@SecurityRequirement(name = "BearerAuth")
public class AdminReviewController {

    private final ReviewUseCase reviewUseCase;

    @Operation(summary = "List all reviews", description = "Moderation listing including reviews hidden from the public catalog.")
    @ApiResponse(responseCode = "200", description = "Review page returned")
    @GetMapping
    public ResponseEntity<Page<ReviewResponse>> listAllReviews(
        @ParameterObject @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(reviewUseCase.listAllReviews(pageable));
    }

    @Operation(summary = "Delete a review", description = "Removes an abusive or non-compliant review from the marketplace.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Review deleted"),
        @ApiResponse(responseCode = "404", description = "Review not found")
    })
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable UUID reviewId) {
        reviewUseCase.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }
}

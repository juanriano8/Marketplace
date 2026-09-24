package com.marketplace.api.modules.review.domain.port.inbound;

import com.marketplace.api.modules.review.application.dto.CreateReviewRequest;
import com.marketplace.api.modules.review.application.dto.ProductReviewsResponse;
import com.marketplace.api.modules.review.application.dto.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ReviewUseCase {

    /** Creates a review, requiring proof of a shipped/delivered purchase. */
    ReviewResponse createReview(UUID buyerId, CreateReviewRequest request);

    /** Public paginated opinions for a product, with aggregate rating. */
    ProductReviewsResponse listProductReviews(UUID productId, Pageable pageable);

    /** Administrator moderation: removes a review from the marketplace. */
    void deleteReview(UUID reviewId);

    /** Administrator moderation listing, includes hidden reviews. */
    Page<ReviewResponse> listAllReviews(Pageable pageable);
}

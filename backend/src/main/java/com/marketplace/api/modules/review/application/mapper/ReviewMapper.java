package com.marketplace.api.modules.review.application.mapper;

import com.marketplace.api.modules.review.application.dto.ReviewResponse;
import com.marketplace.api.modules.review.domain.model.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public ReviewResponse toResponse(Review review) {
        return new ReviewResponse(
            review.getId(),
            review.getProductId(),
            review.getSellerId(),
            review.getBuyerId(),
            review.getRating(),
            review.getTitle(),
            review.getComment(),
            review.isVerifiedPurchase(),
            review.isVisible(),
            review.getCreatedAt()
        );
    }
}

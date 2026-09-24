package com.marketplace.api.modules.review.domain.port.outbound;

import com.marketplace.api.modules.review.domain.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ReviewRepositoryPort {

    Review save(Review review);

    Optional<Review> findById(UUID id);

    void delete(Review review);

    /** Public listing: only visible reviews of the product. */
    Page<Review> findByProductIdAndVisibleTrue(UUID productId, Pageable pageable);

    /** Moderation listing: every review regardless of visibility. */
    Page<Review> findAllIncludingHidden(Pageable pageable);

    boolean existsByBuyerIdAndProductId(UUID buyerId, UUID productId);

    long countByProductIdAndVisibleTrue(UUID productId);

    Double averageRatingForProduct(UUID productId);
}

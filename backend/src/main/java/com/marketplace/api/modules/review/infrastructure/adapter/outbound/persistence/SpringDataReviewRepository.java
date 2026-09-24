package com.marketplace.api.modules.review.infrastructure.adapter.outbound.persistence;

import com.marketplace.api.modules.review.domain.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpringDataReviewRepository extends JpaRepository<Review, UUID> {

    Page<Review> findByProductIdAndVisibleTrue(UUID productId, Pageable pageable);

    boolean existsByBuyerIdAndProductId(UUID buyerId, UUID productId);

    long countByProductIdAndVisibleTrue(UUID productId);

    @Query("select avg(r.rating) from Review r where r.productId = :productId and r.visible = true")
    Double averageRatingForProduct(@Param("productId") UUID productId);
}

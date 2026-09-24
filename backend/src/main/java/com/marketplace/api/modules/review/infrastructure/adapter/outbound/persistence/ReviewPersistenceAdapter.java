package com.marketplace.api.modules.review.infrastructure.adapter.outbound.persistence;

import com.marketplace.api.modules.review.domain.model.Review;
import com.marketplace.api.modules.review.domain.port.outbound.ReviewRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReviewPersistenceAdapter implements ReviewRepositoryPort {

    private final SpringDataReviewRepository springDataReviewRepository;

    @Override
    public Review save(Review review) {
        return springDataReviewRepository.save(review);
    }

    @Override
    public Optional<Review> findById(UUID id) {
        return springDataReviewRepository.findById(id);
    }

    @Override
    public void delete(Review review) {
        springDataReviewRepository.delete(review);
    }

    @Override
    public Page<Review> findByProductIdAndVisibleTrue(UUID productId, Pageable pageable) {
        return springDataReviewRepository.findByProductIdAndVisibleTrue(productId, pageable);
    }

    @Override
    public Page<Review> findAllIncludingHidden(Pageable pageable) {
        return springDataReviewRepository.findAll(pageable);
    }

    @Override
    public boolean existsByBuyerIdAndProductId(UUID buyerId, UUID productId) {
        return springDataReviewRepository.existsByBuyerIdAndProductId(buyerId, productId);
    }

    @Override
    public long countByProductIdAndVisibleTrue(UUID productId) {
        return springDataReviewRepository.countByProductIdAndVisibleTrue(productId);
    }

    @Override
    public Double averageRatingForProduct(UUID productId) {
        return springDataReviewRepository.averageRatingForProduct(productId);
    }
}

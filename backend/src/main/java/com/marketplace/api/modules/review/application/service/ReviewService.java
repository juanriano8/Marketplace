package com.marketplace.api.modules.review.application.service;

import com.marketplace.api.modules.order.domain.model.SubOrder;
import com.marketplace.api.modules.order.domain.model.SubOrderStatus;
import com.marketplace.api.modules.order.domain.port.outbound.SubOrderRepositoryPort;
import com.marketplace.api.modules.product.domain.model.Product;
import com.marketplace.api.modules.product.domain.port.outbound.ProductRepositoryPort;
import com.marketplace.api.modules.review.application.dto.CreateReviewRequest;
import com.marketplace.api.modules.review.application.dto.ProductReviewsResponse;
import com.marketplace.api.modules.review.application.dto.ReviewResponse;
import com.marketplace.api.modules.review.application.mapper.ReviewMapper;
import com.marketplace.api.modules.review.domain.model.Review;
import com.marketplace.api.modules.review.domain.port.inbound.ReviewUseCase;
import com.marketplace.api.modules.review.domain.port.outbound.ReviewRepositoryPort;
import com.marketplace.api.shared.exception.DomainException;
import com.marketplace.api.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Review moderation and creation.
 *
 * <p>A review is only accepted when the buyer has an order that actually reached dispatch
 * ({@code SHIPPED} or {@code DELIVERED}) containing the product, which is what makes the
 * "verified purchase" badge meaningful.</p> */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService implements ReviewUseCase {

    private final ReviewRepositoryPort reviewRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;
    private final SubOrderRepositoryPort subOrderRepositoryPort;
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional
    public ReviewResponse createReview(UUID buyerId, CreateReviewRequest request) {
        Product product = productRepositoryPort.findById(request.productId())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.productId()));

        if (reviewRepositoryPort.existsByBuyerIdAndProductId(buyerId, request.productId())) {
            throw new DomainException("You have already reviewed this product");
        }

        UUID provenSubOrderId = findVerifiedPurchaseSubOrderId(buyerId, request.productId());

        if (provenSubOrderId == null) {
            throw new DomainException(
                "Only buyers with a dispatched or delivered order for this product can review it"
            );
        }

        Review review = new Review(
            product.getId(),
            product.getSellerId(),
            buyerId,
            request.rating(),
            request.title(),
            request.comment(),
            provenSubOrderId
        );

        Review saved = reviewRepositoryPort.save(review);
        log.info("Buyer {} reviewed product {} with rating {} (verified={})",
            buyerId, request.productId(), request.rating(), saved.isVerifiedPurchase());

        return reviewMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductReviewsResponse listProductReviews(UUID productId, Pageable pageable) {
        if (!productRepositoryPort.findById(productId).isPresent()) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        Page<ReviewResponse> page = reviewRepositoryPort.findByProductIdAndVisibleTrue(productId, pageable)
            .map(reviewMapper::toResponse);

        Double average = reviewRepositoryPort.averageRatingForProduct(productId);
        long total = reviewRepositoryPort.countByProductIdAndVisibleTrue(productId);

        return new ProductReviewsResponse(productId, average, total, page);
    }

    @Override
    @Transactional
    public void deleteReview(UUID reviewId) {
        Review review = reviewRepositoryPort.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        reviewRepositoryPort.delete(review);
        log.info("Admin deleted review {}", reviewId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> listAllReviews(Pageable pageable) {
        return reviewRepositoryPort.findAllIncludingHidden(pageable)
            .map(reviewMapper::toResponse);
    }

    /**
     * Looks for an order line the buyer actually received. Ownership is enforced by the query
     * itself ({@code order.buyerId = buyerId}), so a buyer can never verify another buyer's order.
     *
     * @return the sub-order id proving the purchase, or {@code null} when there is no proof
     */
    private UUID findVerifiedPurchaseSubOrderId(UUID buyerId, UUID productId) {
        List<UUID> candidates = new ArrayList<>();
        candidates.addAll(subOrderRepositoryPort.findIdsByBuyerIdAndStatus(buyerId, SubOrderStatus.SHIPPED));
        candidates.addAll(subOrderRepositoryPort.findIdsByBuyerIdAndStatus(buyerId, SubOrderStatus.DELIVERED));

        return subOrderRepositoryPort.findWithItemsByIds(candidates).stream()
            .filter(subOrder -> subOrder.containsProduct(productId))
            .map(SubOrder::getId)
            .findFirst()
            .orElse(null);
    }
}

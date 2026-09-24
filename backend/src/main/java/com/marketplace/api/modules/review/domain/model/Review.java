package com.marketplace.api.modules.review.domain.model;

import com.marketplace.api.shared.domain.BaseEntity;
import com.marketplace.api.shared.exception.DomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * A buyer's opinion about a product they actually purchased.
 *
 * <p>The single-review-per-buyer-per-product rule is enforced by a database unique constraint and
 * checked in the service layer so the failure surfaces as a clear domain error.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "reviews",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_review_buyer_product",
        columnNames = {"buyer_id", "product_id"}
    ),
    indexes = @Index(name = "idx_review_product", columnList = "product_id")
)
public class Review extends BaseEntity {

    public static final int MIN_RATING = 1;
    public static final int MAX_RATING = 5;
    public static final int MAX_COMMENT_LENGTH = 2000;

    @Column(name = "product_id", nullable = false, updatable = false)
    private UUID productId;

    @Column(name = "seller_id", nullable = false, updatable = false)
    private UUID sellerId;

    @Column(name = "buyer_id", nullable = false, updatable = false)
    private UUID buyerId;

    @Column(name = "rating", nullable = false)
    private int rating;

    @Column(name = "title", length = 150)
    private String title;

    @Column(name = "comment", length = MAX_COMMENT_LENGTH)
    private String comment;

    /** Sub-order that proves the purchase; also used to detect "verified purchase" reviews. */
    @Column(name = "sub_order_id", updatable = false)
    private UUID subOrderId;

    @Column(name = "verified_purchase", nullable = false, updatable = false)
    private boolean verifiedPurchase;

    @Column(name = "visible", nullable = false)
    private boolean visible = true;

    public Review(UUID productId, UUID sellerId, UUID buyerId, int rating, String title, String comment,
                  UUID subOrderId) {
        if (productId == null || sellerId == null || buyerId == null) {
            throw new DomainException("A review requires a product, a seller and a buyer");
        }
        if (rating < MIN_RATING || rating > MAX_RATING) {
            throw new DomainException("Rating must be between " + MIN_RATING + " and " + MAX_RATING);
        }
        if (comment != null && comment.length() > MAX_COMMENT_LENGTH) {
            throw new DomainException("Comment must not exceed " + MAX_COMMENT_LENGTH + " characters");
        }

        this.productId = productId;
        this.sellerId = sellerId;
        this.buyerId = buyerId;
        this.rating = rating;
        this.title = title;
        this.comment = comment;
        this.subOrderId = subOrderId;
        this.verifiedPurchase = subOrderId != null;
        this.visible = true;
    }

    /** Moderation action performed by an administrator. */
    public void hide() {
        this.visible = false;
    }

    public boolean isOwnedBy(UUID buyerId) {
        return this.buyerId != null && this.buyerId.equals(buyerId);
    }
}

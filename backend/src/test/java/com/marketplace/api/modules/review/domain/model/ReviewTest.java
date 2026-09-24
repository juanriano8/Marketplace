package com.marketplace.api.modules.review.domain.model;

import com.marketplace.api.shared.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReviewTest {

    private static final UUID PRODUCT_ID = UUID.randomUUID();
    private static final UUID SELLER_ID = UUID.randomUUID();
    private static final UUID BUYER_ID = UUID.randomUUID();
    private static final UUID SUB_ORDER_ID = UUID.randomUUID();

    @Test
    @DisplayName("a review backed by a purchased sub-order is a verified purchase")
    void reviewWithSubOrderIsVerified() {
        Review review = new Review(PRODUCT_ID, SELLER_ID, BUYER_ID, 5, "Great", "Works well", SUB_ORDER_ID);

        assertThat(review.isVerifiedPurchase()).isTrue();
        assertThat(review.getSubOrderId()).isEqualTo(SUB_ORDER_ID);
        assertThat(review.isVisible()).isTrue();
        assertThat(review.isOwnedBy(BUYER_ID)).isTrue();
        assertThat(review.isOwnedBy(UUID.randomUUID())).isFalse();
    }

    @Test
    @DisplayName("a review without a sub-order is not marked as verified")
    void reviewWithoutSubOrderIsNotVerified() {
        Review review = new Review(PRODUCT_ID, SELLER_ID, BUYER_ID, 3, null, null, null);

        assertThat(review.isVerifiedPurchase()).isFalse();
    }

    @ParameterizedTest(name = "rating {0} is rejected")
    @ValueSource(ints = {0, -1, 6, 100})
    @DisplayName("ratings outside 1..5 are rejected")
    void ratingOutOfRangeIsRejected(int rating) {
        assertThatThrownBy(() -> new Review(PRODUCT_ID, SELLER_ID, BUYER_ID, rating, null, null, SUB_ORDER_ID))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("Rating must be between");
    }

    @ParameterizedTest(name = "rating {0} is accepted")
    @ValueSource(ints = {1, 2, 3, 4, 5})
    @DisplayName("ratings within 1..5 are accepted")
    void ratingInRangeIsAccepted(int rating) {
        Review review = new Review(PRODUCT_ID, SELLER_ID, BUYER_ID, rating, null, null, SUB_ORDER_ID);

        assertThat(review.getRating()).isEqualTo(rating);
    }

    @Test
    @DisplayName("an over-long comment is rejected")
    void overLongCommentIsRejected() {
        String tooLong = "x".repeat(Review.MAX_COMMENT_LENGTH + 1);

        assertThatThrownBy(() ->
            new Review(PRODUCT_ID, SELLER_ID, BUYER_ID, 4, "title", tooLong, SUB_ORDER_ID)
        )
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("must not exceed");
    }

    @Test
    @DisplayName("a review requires buyer, product and seller")
    void missingReferencesAreRejected() {
        assertThatThrownBy(() -> new Review(null, SELLER_ID, BUYER_ID, 5, null, null, null))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("requires a product");

        assertThatThrownBy(() -> new Review(PRODUCT_ID, null, BUYER_ID, 5, null, null, null))
            .isInstanceOf(DomainException.class);

        assertThatThrownBy(() -> new Review(PRODUCT_ID, SELLER_ID, null, 5, null, null, null))
            .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("hiding a review removes it from the public catalog")
    void hidingReview() {
        Review review = new Review(PRODUCT_ID, SELLER_ID, BUYER_ID, 1, null, "spam", SUB_ORDER_ID);

        review.hide();

        assertThat(review.isVisible()).isFalse();
    }
}

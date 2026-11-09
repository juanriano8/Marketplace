package com.marketplace.api.modules.seller.domain.model;

import com.marketplace.api.shared.domain.BaseEntity;
import com.marketplace.api.shared.exception.DomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/** Perfil público del vendedor y agregado de sus reseñas. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "seller_profiles",
    uniqueConstraints = @UniqueConstraint(name = "seller_profiles_user_unique", columnNames = "user_id")
)
public class SellerProfile extends BaseEntity {

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "store_name", length = 160)
    private String storeName;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "logo_url", length = 255)
    private String logoUrl;

    @Column(name = "rating_sum", nullable = false)
    private long ratingSum;

    @Column(name = "rating_count", nullable = false)
    private long ratingCount;

    @Column(name = "rating_avg", nullable = false, precision = 3, scale = 2)
    private BigDecimal ratingAvg = BigDecimal.ZERO;

    public SellerProfile(UUID userId, String storeName) {
        if (userId == null) {
            throw new DomainException("A seller profile requires a user");
        }
        this.userId = userId;
        this.storeName = storeName;
    }

    public void update(String storeName, String description, String logoUrl) {
        if (storeName != null) {
            this.storeName = storeName.strip();
        }
        if (description != null) {
            this.description = description.strip();
        }
        if (logoUrl != null) {
            this.logoUrl = logoUrl.strip();
        }
    }

    /**
     * Aplica un delta de reseñas. El agregado nunca queda negativo aunque llegue
     * un evento de borrado duplicado o fuera de orden.
     */
    public void applyRatingDelta(long sumDelta, long countDelta) {
        this.ratingSum = Math.max(0, this.ratingSum + sumDelta);
        this.ratingCount = Math.max(0, this.ratingCount + countDelta);
        this.ratingAvg = ratingCount == 0
            ? BigDecimal.ZERO
            : BigDecimal.valueOf(ratingSum)
                .divide(BigDecimal.valueOf(ratingCount), 2, RoundingMode.HALF_UP);
    }

    public double average() {
        return ratingAvg.doubleValue();
    }
}

package com.marketplace.api.modules.seller.infrastructure.adapter.outbound.persistence;

import com.marketplace.api.modules.seller.domain.model.SellerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataSellerProfileRepository extends JpaRepository<SellerProfile, UUID> {

    Optional<SellerProfile> findByUserId(UUID userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        INSERT INTO seller_profiles
            (id, created_at, updated_at, version, user_id, store_name, description, logo_url,
             rating_sum, rating_count, rating_avg)
        VALUES
            (gen_random_uuid(), now(), now(), 0, :sellerId, NULL, NULL, NULL,
             :sumDelta, :countDelta,
             CASE WHEN :countDelta > 0
                  THEN ROUND(CAST(:sumDelta AS numeric) / CAST(:countDelta AS numeric), 2)
                  ELSE 0 END)
        ON CONFLICT (user_id) DO UPDATE SET
            rating_sum = seller_profiles.rating_sum + EXCLUDED.rating_sum,
            rating_count = seller_profiles.rating_count + EXCLUDED.rating_count,
            rating_avg = CASE
                WHEN seller_profiles.rating_count + EXCLUDED.rating_count > 0
                THEN ROUND(
                    CAST(seller_profiles.rating_sum + EXCLUDED.rating_sum AS numeric)
                    / CAST(seller_profiles.rating_count + EXCLUDED.rating_count AS numeric), 2)
                ELSE 0 END,
            updated_at = now()
        """, nativeQuery = true)
    int applyRatingDelta(@Param("sellerId") UUID sellerId,
                         @Param("sumDelta") long sumDelta,
                         @Param("countDelta") long countDelta);
}

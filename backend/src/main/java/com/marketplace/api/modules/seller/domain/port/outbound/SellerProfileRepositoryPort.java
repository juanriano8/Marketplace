package com.marketplace.api.modules.seller.domain.port.outbound;

import com.marketplace.api.modules.seller.domain.model.SellerProfile;

import java.util.Optional;
import java.util.UUID;

public interface SellerProfileRepositoryPort {

    Optional<SellerProfile> findByUserId(UUID userId);

    SellerProfile save(SellerProfile profile);

    /**
     * Aplica un delta al agregado del vendedor con un upsert atómico.
     * Lo usa el escritor write-behind desde la cola en memoria.
     */
    void applyRatingDelta(UUID sellerId, long sumDelta, long countDelta);
}

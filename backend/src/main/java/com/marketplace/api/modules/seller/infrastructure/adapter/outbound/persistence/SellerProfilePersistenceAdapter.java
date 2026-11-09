package com.marketplace.api.modules.seller.infrastructure.adapter.outbound.persistence;

import com.marketplace.api.modules.seller.domain.model.SellerProfile;
import com.marketplace.api.modules.seller.domain.port.outbound.SellerProfileRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SellerProfilePersistenceAdapter implements SellerProfileRepositoryPort {

    private final SpringDataSellerProfileRepository repository;

    @Override
    public Optional<SellerProfile> findByUserId(UUID userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public SellerProfile save(SellerProfile profile) {
        return repository.save(profile);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void applyRatingDelta(UUID sellerId, long sumDelta, long countDelta) {
        repository.applyRatingDelta(sellerId, sumDelta, countDelta);
    }
}

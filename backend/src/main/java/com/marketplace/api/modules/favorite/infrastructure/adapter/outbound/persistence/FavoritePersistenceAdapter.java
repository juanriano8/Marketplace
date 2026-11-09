package com.marketplace.api.modules.favorite.infrastructure.adapter.outbound.persistence;

import com.marketplace.api.modules.favorite.domain.model.Favorite;
import com.marketplace.api.modules.favorite.domain.port.outbound.FavoriteRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FavoritePersistenceAdapter implements FavoriteRepositoryPort {

    private final SpringDataFavoriteRepository repository;

    @Override
    public Favorite save(Favorite favorite) {
        return repository.save(favorite);
    }

    @Override
    public boolean existsByUserIdAndProductId(UUID userId, UUID productId) {
        return repository.existsByUserIdAndProductId(userId, productId);
    }

    @Override
    public Optional<Favorite> findByUserIdAndProductId(UUID userId, UUID productId) {
        return repository.findByUserIdAndProductId(userId, productId);
    }

    @Override
    public void delete(Favorite favorite) {
        repository.delete(favorite);
    }

    @Override
    public List<UUID> findProductIdsByUserId(UUID userId) {
        return repository.findProductIdsByUserId(userId);
    }
}

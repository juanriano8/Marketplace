package com.marketplace.api.modules.favorite.domain.port.outbound;

import com.marketplace.api.modules.favorite.domain.model.Favorite;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FavoriteRepositoryPort {

    Favorite save(Favorite favorite);

    boolean existsByUserIdAndProductId(UUID userId, UUID productId);

    Optional<Favorite> findByUserIdAndProductId(UUID userId, UUID productId);

    void delete(Favorite favorite);

    List<UUID> findProductIdsByUserId(UUID userId);
}

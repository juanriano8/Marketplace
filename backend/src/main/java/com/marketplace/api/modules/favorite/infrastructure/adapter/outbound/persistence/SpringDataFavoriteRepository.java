package com.marketplace.api.modules.favorite.infrastructure.adapter.outbound.persistence;

import com.marketplace.api.modules.favorite.domain.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataFavoriteRepository extends JpaRepository<Favorite, UUID> {

    boolean existsByUserIdAndProductId(UUID userId, UUID productId);

    Optional<Favorite> findByUserIdAndProductId(UUID userId, UUID productId);

    @Query("""
        select f.productId
          from Favorite f
         where f.userId = :userId
         order by f.createdAt desc
        """)
    List<UUID> findProductIdsByUserId(@Param("userId") UUID userId);
}

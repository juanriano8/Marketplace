package com.marketplace.api.modules.product.infrastructure.adapter.outbound.persistence;

import com.marketplace.api.modules.product.domain.model.Product;
import com.marketplace.api.modules.product.domain.model.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataProductRepository extends JpaRepository<Product, UUID> {
    Optional<Product> findBySlug(String slug);
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);
    Page<Product> findBySellerIdAndStatus(UUID sellerId, ProductStatus status, Pageable pageable);
    Page<Product> findBySellerId(UUID sellerId, Pageable pageable);
    boolean existsBySlug(String slug);

    /**
     * Ranking público: promedio de reseñas (0 si no hay) multiplicado por un decaimiento
     * exponencial con vida media de 30 días desde la creación del producto.
     */
    @Query(value = """
        SELECT p.*
          FROM products p
         WHERE p.status = 'ACTIVE'
         ORDER BY (
             COALESCE((SELECT AVG(r.rating)
                         FROM reviews r
                        WHERE r.product_id = p.id
                          AND r.visible = true), 0) + 1
         ) * EXP(-EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - p.created_at)) / 2592000.0) DESC,
         p.created_at DESC
         LIMIT :limit
        """, nativeQuery = true)
    List<Product> findRankedActive(@Param("limit") int limit);
}

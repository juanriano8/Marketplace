package com.marketplace.api.modules.inventory.infrastructure.adapter.outbound.persistence;

import com.marketplace.api.modules.inventory.domain.model.StockItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataStockRepository extends JpaRepository<StockItem, UUID> {

    Optional<StockItem> findByProductId(UUID productId);

    /**
     * Reads the stock row with a {@code SELECT ... FOR UPDATE} pessimistic write lock.
     *
     * <p>Required by the technical specification ("Control de bloqueos JPA (Pessimistic Locking) al
     * reservar productos en Cloud SQL"): while a checkout holds this row, any other checkout for the
     * same product waits at the database instead of failing, which is what stops two buyers from
     * reserving the same last unit.</p>
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from StockItem s where s.productId = :productId")
    Optional<StockItem> findByProductIdForUpdate(@Param("productId") UUID productId);

    Page<StockItem> findBySellerId(UUID sellerId, Pageable pageable);

    boolean existsByProductId(UUID productId);
}

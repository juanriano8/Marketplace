package com.marketplace.api.modules.inventory.infrastructure.adapter.outbound.persistence;

import com.marketplace.api.modules.inventory.domain.model.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpringDataStockMovementRepository extends JpaRepository<StockMovement, UUID> {

    Page<StockMovement> findByProductId(UUID productId, Pageable pageable);

    Page<StockMovement> findBySellerId(UUID sellerId, Pageable pageable);
}

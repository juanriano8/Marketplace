package com.marketplace.api.modules.inventory.domain.port.outbound;

import com.marketplace.api.modules.inventory.domain.model.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface StockMovementRepositoryPort {

    StockMovement save(StockMovement movement);

    /** Global audit trail, newest first (ordering supplied through {@link Pageable}). */
    Page<StockMovement> findAll(Pageable pageable);

    Page<StockMovement> findByProductId(UUID productId, Pageable pageable);

    Page<StockMovement> findBySellerId(UUID sellerId, Pageable pageable);
}

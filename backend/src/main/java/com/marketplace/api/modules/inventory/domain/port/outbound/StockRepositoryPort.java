package com.marketplace.api.modules.inventory.domain.port.outbound;

import com.marketplace.api.modules.inventory.domain.model.StockItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface StockRepositoryPort {

    StockItem save(StockItem stockItem);

    Optional<StockItem> findByProductId(UUID productId);

    /**
     * Same lookup, but taking a pessimistic write lock on the row.
     *
     * <p>Used by the checkout reservation path so that concurrent checkouts of the same product are
     * serialized by the database (Cloud SQL) instead of racing.</p>
     */
    Optional<StockItem> findByProductIdForUpdate(UUID productId);

    /** Seller-scoped stock listing. */
    Page<StockItem> findBySellerId(UUID sellerId, Pageable pageable);

    /** Global stock listing for the administrative audit view. */
    Page<StockItem> findAll(Pageable pageable);

    boolean existsByProductId(UUID productId);
}

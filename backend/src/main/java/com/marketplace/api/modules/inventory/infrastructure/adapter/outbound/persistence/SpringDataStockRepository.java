package com.marketplace.api.modules.inventory.infrastructure.adapter.outbound.persistence;

import com.marketplace.api.modules.inventory.domain.model.StockItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataStockRepository extends JpaRepository<StockItem, UUID> {

    Optional<StockItem> findByProductId(UUID productId);

    Page<StockItem> findBySellerId(UUID sellerId, Pageable pageable);

    boolean existsByProductId(UUID productId);
}

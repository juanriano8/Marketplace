package com.marketplace.api.modules.inventory.infrastructure.adapter.outbound.persistence;

import com.marketplace.api.modules.inventory.domain.model.StockMovement;
import com.marketplace.api.modules.inventory.domain.port.outbound.StockMovementRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StockMovementPersistenceAdapter implements StockMovementRepositoryPort {

    private final SpringDataStockMovementRepository springDataStockMovementRepository;

    @Override
    public StockMovement save(StockMovement movement) {
        return springDataStockMovementRepository.save(movement);
    }

    @Override
    public Page<StockMovement> findAll(Pageable pageable) {
        return springDataStockMovementRepository.findAll(pageable);
    }

    @Override
    public Page<StockMovement> findByProductId(UUID productId, Pageable pageable) {
        return springDataStockMovementRepository.findByProductId(productId, pageable);
    }

    @Override
    public Page<StockMovement> findBySellerId(UUID sellerId, Pageable pageable) {
        return springDataStockMovementRepository.findBySellerId(sellerId, pageable);
    }
}

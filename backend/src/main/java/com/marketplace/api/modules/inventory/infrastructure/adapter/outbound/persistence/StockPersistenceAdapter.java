package com.marketplace.api.modules.inventory.infrastructure.adapter.outbound.persistence;

import com.marketplace.api.modules.inventory.domain.model.StockItem;
import com.marketplace.api.modules.inventory.domain.port.outbound.StockRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StockPersistenceAdapter implements StockRepositoryPort {

    private final SpringDataStockRepository springDataStockRepository;

    @Override
    public StockItem save(StockItem stockItem) {
        return springDataStockRepository.save(stockItem);
    }

    @Override
    public Optional<StockItem> findByProductId(UUID productId) {
        return springDataStockRepository.findByProductId(productId);
    }

    @Override
    public Optional<StockItem> findByProductIdForUpdate(UUID productId) {
        return springDataStockRepository.findByProductIdForUpdate(productId);
    }

    @Override
    public Page<StockItem> findBySellerId(UUID sellerId, Pageable pageable) {
        return springDataStockRepository.findBySellerId(sellerId, pageable);
    }

    @Override
    public Page<StockItem> findAll(Pageable pageable) {
        return springDataStockRepository.findAll(pageable);
    }

    @Override
    public boolean existsByProductId(UUID productId) {
        return springDataStockRepository.existsByProductId(productId);
    }
}

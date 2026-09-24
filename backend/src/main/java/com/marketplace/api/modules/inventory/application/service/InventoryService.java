package com.marketplace.api.modules.inventory.application.service;

import com.marketplace.api.modules.inventory.application.dto.SellerStockResponse;
import com.marketplace.api.modules.inventory.application.dto.StockAdjustmentRequest;
import com.marketplace.api.modules.inventory.application.dto.StockAvailabilityResponse;
import com.marketplace.api.modules.inventory.application.dto.StockMovementResponse;
import com.marketplace.api.modules.inventory.application.mapper.InventoryMapper;
import com.marketplace.api.modules.inventory.domain.model.StockItem;
import com.marketplace.api.modules.inventory.domain.model.StockMovement;
import com.marketplace.api.modules.inventory.domain.model.StockMovementType;
import com.marketplace.api.modules.inventory.domain.model.StockReservation;
import com.marketplace.api.modules.inventory.domain.port.inbound.InventoryUseCase;
import com.marketplace.api.modules.inventory.domain.port.inbound.StockQueryPort;
import com.marketplace.api.modules.inventory.domain.port.outbound.StockMovementRepositoryPort;
import com.marketplace.api.modules.inventory.domain.port.outbound.StockRepositoryPort;
import com.marketplace.api.shared.exception.DomainException;
import com.marketplace.api.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService implements InventoryUseCase, StockQueryPort {

    private final StockRepositoryPort stockRepositoryPort;
    private final StockMovementRepositoryPort stockMovementRepositoryPort;
    private final InventoryMapper inventoryMapper;

    @Override
    @Transactional
    public void initialiseStock(UUID productId, UUID sellerId, int initialQuantity) {
        if (stockRepositoryPort.existsByProductId(productId)) {
            log.debug("Stock already initialised for product {}", productId);
            return;
        }

        StockItem stockItem = stockRepositoryPort.save(new StockItem(productId, sellerId, initialQuantity));
        recordMovement(stockItem, StockMovementType.INITIAL, 0, initialQuantity,
            "Initial stock declared when the product was created", sellerId, null);

        log.info("Initialised stock for product {} at {} units", productId, initialQuantity);
    }

    @Override
    @Transactional
    public SellerStockResponse adjustStock(UUID sellerId, StockAdjustmentRequest request) {
        boolean hasDelta = request.hasDelta();
        boolean hasAbsolute = request.hasPhysicalQuantity();

        if (hasDelta == hasAbsolute) {
            throw new DomainException(
                "Provide exactly one of quantityDelta or physicalQuantity to adjust stock"
            );
        }

        StockItem stockItem = stockRepositoryPort.findByProductId(request.productId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "No stock record found for product id: " + request.productId()
            ));

        // BOLA check — a seller may only adjust stock they own.
        if (!stockItem.isOwnedBy(sellerId)) {
            throw new DomainException("You are not authorized to adjust stock for this product");
        }

        int before = stockItem.getAvailableQuantity();

        if (hasDelta) {
            stockItem.adjust(request.quantityDelta());
        } else {
            stockItem.setPhysicalQuantity(request.physicalQuantity());
        }

        int after = stockItem.getAvailableQuantity();
        StockItem saved = stockRepositoryPort.save(stockItem);
        recordMovement(saved, StockMovementType.ADJUSTMENT, before, after,
            request.reason(), sellerId, null);

        log.info("Seller {} adjusted stock of product {} from {} to {}", sellerId, request.productId(), before, after);
        return inventoryMapper.toSellerStock(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SellerStockResponse> listSellerStock(UUID sellerId, Pageable pageable) {
        return stockRepositoryPort.findBySellerId(sellerId, pageable)
            .map(inventoryMapper::toSellerStock);
    }

    @Override
    @Transactional(readOnly = true)
    public StockAvailabilityResponse checkAvailability(UUID productId) {
        return inventoryMapper.toAvailability(requireStock(productId));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StockAvailabilityResponse> findAvailability(UUID productId) {
        return stockRepositoryPort.findByProductId(productId)
            .map(inventoryMapper::toAvailability);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasSellableStock(UUID productId, int quantity) {
        return stockRepositoryPort.findByProductId(productId)
            .map(item -> item.canFulfil(quantity))
            .orElse(false);
    }

    @Override
    @Transactional
    public void reserveStock(UUID productId, UUID sellerId, int quantity, String reference) {
        StockItem stockItem = requireStock(productId);

        if (!stockItem.isOwnedBy(sellerId)) {
            throw new DomainException(
                "Stock record for product " + productId + " does not belong to seller " + sellerId
            );
        }

        int before = stockItem.getAvailableQuantity();
        stockItem.reserve(quantity);

        StockItem saved = stockRepositoryPort.save(stockItem);
        recordMovement(saved, StockMovementType.RESERVATION, before, saved.getAvailableQuantity(),
            "Stock held for checkout " + reference, sellerId, reference);
    }

    @Override
    @Transactional
    public void releaseReservation(StockReservation reservation, String reference) {
        StockItem stockItem = requireStock(reservation.productId());

        int before = stockItem.getAvailableQuantity();
        stockItem.releaseReserved(reservation.quantity());

        StockItem saved = stockRepositoryPort.save(stockItem);
        recordMovement(saved, StockMovementType.CANCELLATION, before, saved.getAvailableQuantity(),
            "Reservation released for " + reference, saved.getSellerId(), reference);
    }

    @Override
    @Transactional
    public void commitReservation(StockReservation reservation, String reference) {
        StockItem stockItem = requireStock(reservation.productId());

        int before = stockItem.getAvailableQuantity();
        stockItem.commitReserved(reservation.quantity());

        StockItem saved = stockRepositoryPort.save(stockItem);
        recordMovement(saved, StockMovementType.SALE, before, saved.getAvailableQuantity(),
            "Reservation consumed by " + reference, saved.getSellerId(), reference);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StockMovementResponse> auditMovements(Pageable pageable) {
        return stockMovementRepositoryPort.findAll(pageable)
            .map(inventoryMapper::toMovement);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StockMovementResponse> auditMovementsByProduct(UUID productId, Pageable pageable) {
        return stockMovementRepositoryPort.findByProductId(productId, pageable)
            .map(inventoryMapper::toMovement);
    }

    private StockItem requireStock(UUID productId) {
        return stockRepositoryPort.findByProductId(productId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No stock record found for product id: " + productId
            ));
    }

    private void recordMovement(StockItem stockItem, StockMovementType type, int before, int after,
                                String reason, UUID performedBy, String reference) {
        stockMovementRepositoryPort.save(new StockMovement(
            stockItem.getProductId(),
            stockItem.getSellerId(),
            type,
            before,
            after,
            reason,
            performedBy,
            reference
        ));
    }
}

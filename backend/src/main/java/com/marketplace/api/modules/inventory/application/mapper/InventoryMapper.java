package com.marketplace.api.modules.inventory.application.mapper;

import com.marketplace.api.modules.inventory.application.dto.SellerStockResponse;
import com.marketplace.api.modules.inventory.application.dto.StockAvailabilityResponse;
import com.marketplace.api.modules.inventory.application.dto.StockMovementResponse;
import com.marketplace.api.modules.inventory.domain.model.StockItem;
import com.marketplace.api.modules.inventory.domain.model.StockMovement;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper {

    public StockAvailabilityResponse toAvailability(StockItem item) {
        return new StockAvailabilityResponse(
            item.getProductId(),
            item.getSellerId(),
            item.getAvailableQuantity(),
            item.getReservedQuantity(),
            item.getSellableQuantity(),
            item.getLowStockThreshold(),
            item.isLowStock(),
            item.canFulfil(1),
            item.getLastAdjustedAt()
        );
    }

    public SellerStockResponse toSellerStock(StockItem item) {
        return new SellerStockResponse(
            item.getId(),
            item.getProductId(),
            item.getSellerId(),
            item.getAvailableQuantity(),
            item.getReservedQuantity(),
            item.getSellableQuantity(),
            item.getLowStockThreshold(),
            item.isLowStock(),
            item.getLastAdjustedAt()
        );
    }

    public StockMovementResponse toMovement(StockMovement movement) {
        return new StockMovementResponse(
            movement.getId(),
            movement.getProductId(),
            movement.getSellerId(),
            movement.getMovementType(),
            movement.getQuantityDelta(),
            movement.getQuantityBefore(),
            movement.getQuantityAfter(),
            movement.getReason(),
            movement.getPerformedBy(),
            movement.getReference(),
            movement.getCreatedAt()
        );
    }
}

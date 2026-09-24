package com.marketplace.api.modules.inventory.domain.port.inbound;

import com.marketplace.api.modules.inventory.application.dto.StockAdjustmentRequest;
import com.marketplace.api.modules.inventory.application.dto.SellerStockResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Inbound port for the inventory module.
 */
public interface InventoryUseCase {

    /** Creates the opening ledger entry when a product is published. */
    void initialiseStock(UUID productId, UUID sellerId, int initialQuantity);

    /** Seller correction of physical stock; recorded in the audit trail. */
    SellerStockResponse adjustStock(UUID sellerId, StockAdjustmentRequest request);

    Page<SellerStockResponse> listSellerStock(UUID sellerId, Pageable pageable);
}

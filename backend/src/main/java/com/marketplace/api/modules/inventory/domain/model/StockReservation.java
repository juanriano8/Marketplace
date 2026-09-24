package com.marketplace.api.modules.inventory.domain.model;

import java.util.UUID;

/**
 * A stock reservation that was successfully taken for a pending checkout and must later be either
 * committed (payment captured) or released (payment failed / order cancelled).
 */
public record StockReservation(UUID productId, UUID sellerId, int quantity) {
}

package com.marketplace.api.modules.inventory.domain.port.inbound;

import com.marketplace.api.modules.inventory.application.dto.StockAvailabilityResponse;
import com.marketplace.api.modules.inventory.application.dto.StockMovementResponse;
import com.marketplace.api.modules.inventory.domain.model.StockReservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

/**
 * Read/commit port consumed by other modules (cart, checkout, review) and by the public
 * availability and administrative audit endpoints.
 */
public interface StockQueryPort {

    StockAvailabilityResponse checkAvailability(UUID productId);

    /**
     * Non-throwing variant used when enriching other responses (for example the product catalog),
     * where a missing stock record must not be treated as an error.
     */
    Optional<StockAvailabilityResponse> findAvailability(UUID productId);

    /**
     * @return true when {@code quantity} units of the product can currently be sold
     */
    boolean hasSellableStock(UUID productId, int quantity);

    /**
     * Takes a reservation so the units cannot be sold twice while the payment is in flight.
     *
     * @throws com.marketplace.api.shared.exception.DomainException when stock is insufficient
     */
    void reserveStock(UUID productId, UUID sellerId, int quantity, String reference);

    /** Releases one previously taken reservation (payment failed / order cancelled). */
    void releaseReservation(StockReservation reservation, String reference);

    /** Consumes one previously taken reservation (payment captured). */
    void commitReservation(StockReservation reservation, String reference);

    Page<StockMovementResponse> auditMovements(Pageable pageable);

    Page<StockMovementResponse> auditMovementsByProduct(UUID productId, Pageable pageable);
}

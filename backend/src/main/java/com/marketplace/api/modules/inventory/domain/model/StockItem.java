package com.marketplace.api.modules.inventory.domain.model;

import com.marketplace.api.shared.domain.BaseEntity;
import com.marketplace.api.shared.exception.DomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Authoritative stock record for a product.
 *
 * <p>One row per product. Optimistic locking comes from {@link BaseEntity#getVersion()} so that
 * concurrent checkouts fail loudly instead of overselling the last units.</p>
 */
@Getter
@NoArgsConstructor
@Entity
@Table(
    name = "stock_items",
    uniqueConstraints = @UniqueConstraint(name = "uk_stock_item_product", columnNames = "product_id")
)
public class StockItem extends BaseEntity {

    public static final int DEFAULT_LOW_STOCK_THRESHOLD = 10;

    @Column(name = "product_id", nullable = false, updatable = false)
    private UUID productId;

    @Column(name = "seller_id", nullable = false, updatable = false)
    private UUID sellerId;

    @Column(name = "available_quantity", nullable = false)
    private int availableQuantity;

    @Column(name = "reserved_quantity", nullable = false)
    private int reservedQuantity;

    @Column(name = "low_stock_threshold", nullable = false)
    private int lowStockThreshold;

    @Column(name = "last_adjusted_at")
    private Instant lastAdjustedAt;

    public StockItem(UUID productId, UUID sellerId, int initialQuantity) {
        if (productId == null) {
            throw new DomainException("Stock requires a product");
        }
        if (sellerId == null) {
            throw new DomainException("Stock requires a seller");
        }
        if (initialQuantity < 0) {
            throw new DomainException("Initial stock cannot be negative");
        }
        this.productId = productId;
        this.sellerId = sellerId;
        this.availableQuantity = initialQuantity;
        this.reservedQuantity = 0;
        this.lowStockThreshold = DEFAULT_LOW_STOCK_THRESHOLD;
        this.lastAdjustedAt = Instant.now();
    }

    /**
     * Applies a signed physical-count delta. The justification for the change is recorded by the
     * caller as a {@link StockMovement} audit entry.
     *
     * @param delta positive to add units, negative to remove them
     */
    public void adjust(int delta) {
        if (delta == 0) {
            throw new DomainException("Stock adjustment must be different from zero");
        }
        int resulting = this.availableQuantity + delta;
        if (resulting < 0) {
            throw new DomainException(
                "Adjustment would leave a negative stock level (" + resulting + "); current available is "
                    + this.availableQuantity
            );
        }
        this.availableQuantity = resulting;
        this.lastAdjustedAt = Instant.now();
    }

    /** Absolute recount: sets the physical quantity regardless of the previous value. */
    public void setPhysicalQuantity(int quantity) {
        if (quantity < 0) {
            throw new DomainException("Physical quantity cannot be negative");
        }
        this.availableQuantity = quantity;
        this.lastAdjustedAt = Instant.now();
    }

    /**
     * Commits {@code quantity} units to a paid order, reducing sellable stock.
     */
    public void reserve(int quantity) {
        if (quantity <= 0) {
            throw new DomainException("Reserved quantity must be greater than zero");
        }
        if (quantity > getSellableQuantity()) {
            throw new DomainException(
                "Insufficient stock: requested " + quantity + " but only " + getSellableQuantity() + " available"
            );
        }
        this.reservedQuantity += quantity;
        this.lastAdjustedAt = Instant.now();
    }

    /**
     * Releases previously reserved units (cancellation/refund) without consuming them, so they
     * become sellable again. The physical count is left untouched.
     */
    public void releaseReserved(int quantity) {
        if (quantity <= 0) {
            throw new DomainException("Released quantity must be greater than zero");
        }
        int released = Math.min(quantity, this.reservedQuantity);
        this.reservedQuantity -= released;
        this.lastAdjustedAt = Instant.now();
    }

    /**
     * Consumes {@code quantity} units that were previously reserved, so they can no longer be sold.
     */
    public void commitReserved(int quantity) {
        if (quantity <= 0) {
            throw new DomainException("Committed quantity must be greater than zero");
        }
        if (quantity > this.reservedQuantity) {
            throw new DomainException(
                "Cannot commit " + quantity + " units: only " + this.reservedQuantity + " are reserved"
            );
        }
        this.reservedQuantity -= quantity;
        this.availableQuantity = Math.max(0, this.availableQuantity - quantity);
        this.lastAdjustedAt = Instant.now();
    }

    public void updateLowStockThreshold(int threshold) {
        if (threshold < 0) {
            throw new DomainException("Low stock threshold cannot be negative");
        }
        this.lowStockThreshold = threshold;
    }

    /** Units that may still be promised to buyers. */
    public int getSellableQuantity() {
        return Math.max(0, this.availableQuantity - this.reservedQuantity);
    }

    public boolean isLowStock() {
        return getSellableQuantity() <= this.lowStockThreshold;
    }

    public boolean canFulfil(int quantity) {
        return quantity > 0 && quantity <= getSellableQuantity();
    }

    public boolean isOwnedBy(UUID sellerId) {
        return this.sellerId != null && this.sellerId.equals(sellerId);
    }
}

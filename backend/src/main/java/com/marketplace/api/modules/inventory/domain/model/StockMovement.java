package com.marketplace.api.modules.inventory.domain.model;

import com.marketplace.api.shared.domain.BaseEntity;
import com.marketplace.api.shared.exception.DomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Append-only audit entry for every stock mutation.
 *
 * <p>Entries are never updated or deleted, which is what makes
 * {@code GET /api/v1/admin/inventory/audit} trustworthy as an audit trail.</p>
 */
@Getter
@NoArgsConstructor
@Entity
@Table(
    name = "stock_movements",
    indexes = {
        @Index(name = "idx_stock_movement_product", columnList = "product_id"),
        @Index(name = "idx_stock_movement_seller", columnList = "seller_id")
    }
)
public class StockMovement extends BaseEntity {

    @Column(name = "product_id", nullable = false, updatable = false)
    private UUID productId;

    @Column(name = "seller_id", nullable = false, updatable = false)
    private UUID sellerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 30, updatable = false)
    private StockMovementType movementType;

    @Column(name = "quantity_delta", nullable = false, updatable = false)
    private int quantityDelta;

    @Column(name = "quantity_before", nullable = false, updatable = false)
    private int quantityBefore;

    @Column(name = "quantity_after", nullable = false, updatable = false)
    private int quantityAfter;

    @Column(name = "reason", length = 500, updatable = false)
    private String reason;

    /** Who triggered the movement: seller, admin, or a system process (null for system). */
    @Column(name = "performed_by", updatable = false)
    private UUID performedBy;

    @Column(name = "reference", length = 120, updatable = false)
    private String reference;

    public StockMovement(UUID productId, UUID sellerId, StockMovementType movementType,
                         int quantityBefore, int quantityAfter, String reason,
                         UUID performedBy, String reference) {
        if (productId == null || sellerId == null) {
            throw new DomainException("A stock movement requires a product and a seller");
        }
        if (movementType == null) {
            throw new DomainException("A stock movement requires a type");
        }
        this.productId = productId;
        this.sellerId = sellerId;
        this.movementType = movementType;
        this.quantityBefore = quantityBefore;
        this.quantityAfter = quantityAfter;
        this.quantityDelta = quantityAfter - quantityBefore;
        this.reason = reason;
        this.performedBy = performedBy;
        this.reference = reference;
    }
}

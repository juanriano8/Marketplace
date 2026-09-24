package com.marketplace.api.modules.order.domain.model;

import com.marketplace.api.shared.domain.BaseEntity;
import com.marketplace.api.shared.exception.DomainException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * The portion of an {@link Order} that belongs to a single seller.
 *
 * <p>Sellers only ever see and mutate their own sub-orders, which is what keeps one seller from
 * touching another seller's fulfilment data (horizontal privilege escalation / BOLA).</p>
 */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "sub_orders")
public class SubOrder extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, updatable = false)
    private Order order;

    @Column(name = "seller_id", nullable = false, updatable = false)
    private UUID sellerId;

    @Column(name = "subtotal", nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SubOrderStatus status;

    @Column(name = "tracking_number")
    private String trackingNumber;

    @Column(name = "carrier")
    private String carrier;

    @Column(name = "shipped_at")
    private Instant shippedAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @BatchSize(size = 50)
    @OneToMany(
        mappedBy = "subOrder",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    private List<OrderItem> items = new ArrayList<>();

    public SubOrder(Order order, UUID sellerId, String currencyCode) {
        if (sellerId == null) {
            throw new DomainException("A sub-order requires a seller");
        }
        this.order = order;
        this.sellerId = sellerId;
        this.currencyCode = currencyCode != null ? currencyCode : "USD";
        this.status = SubOrderStatus.PENDING;
        this.subtotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Appends a purchased line, snapshotting name and price so later catalog edits cannot change
     * what the buyer actually bought.
     */
    public OrderItem addItem(UUID productId, String productName, int quantity,
                             BigDecimal unitPrice, String currencyCode) {
        if (quantity <= 0) {
            throw new DomainException("Order item quantity must be greater than zero");
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new DomainException("Order item unit price cannot be negative");
        }
        if (this.currencyCode != null && currencyCode != null && !this.currencyCode.equals(currencyCode)) {
            throw new DomainException(
                "Cannot mix currencies inside a single order: expected " + this.currencyCode + " but got " + currencyCode
            );
        }

        OrderItem item = new OrderItem(
            this,
            productId,
            productName,
            quantity,
            unitPrice.setScale(2, RoundingMode.HALF_UP),
            currencyCode != null ? currencyCode : this.currencyCode
        );
        items.add(item);
        recalculateSubtotal();
        return item;
    }

    public void recalculateSubtotal() {
        this.subtotal = items.stream()
            .map(OrderItem::lineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
    }

    /** Moves a freshly-created sub-order into processing once payment succeeds. */
    public void markProcessing() {
        if (this.status == SubOrderStatus.PENDING) {
            this.status = SubOrderStatus.PROCESSING;
        }
    }

    /**
     * Records dispatch information. Requires at least a tracking number, matching the seller
     * "actualizar despacho" contract.
     */
    public void ship(String trackingNumber, String carrier) {
        if (trackingNumber == null || trackingNumber.isBlank()) {
            throw new DomainException("A tracking number is required to mark an order as shipped");
        }
        if (this.status != SubOrderStatus.PROCESSING) {
            throw new DomainException(
                "Only sub-orders in PROCESSING can be shipped, current status is " + this.status
            );
        }
        this.status = SubOrderStatus.SHIPPED;
        this.trackingNumber = trackingNumber.trim();
        this.carrier = carrier != null ? carrier.trim() : null;
        this.shippedAt = Instant.now();
    }

    public void markDelivered() {
        if (this.status != SubOrderStatus.SHIPPED) {
            throw new DomainException("Only shipped sub-orders can be marked as delivered");
        }
        this.status = SubOrderStatus.DELIVERED;
        this.deliveredAt = Instant.now();
    }

    public void cancel() {
        if (this.status == SubOrderStatus.DELIVERED) {
            throw new DomainException("Delivered sub-orders cannot be cancelled");
        }
        this.status = SubOrderStatus.CANCELLED;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public boolean isOwnedBy(UUID sellerId) {
        return this.sellerId != null && this.sellerId.equals(sellerId);
    }

    public boolean containsProduct(UUID productId) {
        return items.stream().anyMatch(item -> item.getProductId().equals(productId));
    }

    public boolean isPending() {
        return this.status == SubOrderStatus.PENDING;
    }

    public boolean isShipped() {
        return this.status == SubOrderStatus.SHIPPED;
    }

    public boolean isDelivered() {
        return this.status == SubOrderStatus.DELIVERED;
    }
}

package com.marketplace.api.modules.order.domain.model;

import com.marketplace.api.shared.domain.BaseEntity;
import com.marketplace.api.shared.domain.Money;
import com.marketplace.api.shared.exception.DomainException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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
import java.util.Currency;
import java.util.List;
import java.util.UUID;

/**
 * Order aggregate root.
 *
 * <p>An order is created from a buyer's cart and is split into one {@link SubOrder} per seller so
 * that each seller fulfils and ships independently while the buyer sees (and pays for) a single
 * transaction.</p>
 */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order extends BaseEntity {

    @Column(name = "order_number", nullable = false, unique = true, updatable = false)
    private String orderNumber;

    @Column(name = "buyer_id", nullable = false, updatable = false)
    private UUID buyerId;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OrderStatus status;

    @Column(name = "payment_reference")
    private String paymentReference;

    @Column(name = "paid_at")
    private Instant paidAt;

    @BatchSize(size = 50)
    @OneToMany(
        mappedBy = "order",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    private List<SubOrder> subOrders = new ArrayList<>();

    public Order(UUID buyerId, String currencyCode) {
        if (buyerId == null) {
            throw new DomainException("An order requires a buyer");
        }
        this.buyerId = buyerId;
        this.currencyCode = currencyCode != null ? currencyCode : "USD";
        this.status = OrderStatus.PENDING_PAYMENT;
        this.totalAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        this.orderNumber = generateOrderNumber();
    }

    /**
     * Adds the seller-scoped portion of the order and links it back to this aggregate.
     */
    public SubOrder addSubOrder(UUID sellerId) {
        SubOrder subOrder = new SubOrder(this, sellerId, this.currencyCode);
        subOrders.add(subOrder);
        return subOrder;
    }

    /**
     * Recomputes the order total from its sub-orders. Called after all lines are added.
     */
    public void recalculateTotal() {
        this.totalAmount = subOrders.stream()
            .map(SubOrder::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);

        if (this.totalAmount.signum() <= 0) {
            throw new DomainException("Order total must be greater than zero");
        }
    }

    public void markPaid(String paymentReference) {
        if (this.status != OrderStatus.PENDING_PAYMENT) {
            throw new DomainException("Only orders pending payment can be marked as paid");
        }
        this.status = OrderStatus.PAID;
        this.paymentReference = paymentReference;
        this.paidAt = Instant.now();
        this.subOrders.forEach(SubOrder::markProcessing);
    }

    public void markPaymentFailed(String detail) {
        if (this.status != OrderStatus.PENDING_PAYMENT) {
            throw new DomainException("Only orders pending payment can fail payment");
        }
        this.status = OrderStatus.PAYMENT_FAILED;
        this.paymentReference = detail;
    }

    public void cancel() {
        if (this.status == OrderStatus.CANCELLED) {
            return;
        }
        if (this.status == OrderStatus.COMPLETED) {
            throw new DomainException("Completed orders cannot be cancelled");
        }
        this.status = OrderStatus.CANCELLED;
        this.subOrders.forEach(SubOrder::cancel);
    }

    /**
     * Closes the order once every sub-order has been delivered.
     */
    public void completeIfFullyDelivered() {
        if (this.status == OrderStatus.PAID && !subOrders.isEmpty()
            && subOrders.stream().allMatch(s -> s.getStatus() == SubOrderStatus.DELIVERED)) {
            this.status = OrderStatus.COMPLETED;
        }
    }

    public List<SubOrder> getSubOrders() {
        return Collections.unmodifiableList(subOrders);
    }

    public Money totalAsMoney() {
        return Money.of(totalAmount, Currency.getInstance(currencyCode));
    }

    public boolean isOwnedBy(UUID buyerId) {
        return this.buyerId != null && this.buyerId.equals(buyerId);
    }

    public boolean hasSeller(UUID sellerId) {
        return subOrders.stream().anyMatch(sub -> sub.isOwnedBy(sellerId));
    }

    private static String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
}

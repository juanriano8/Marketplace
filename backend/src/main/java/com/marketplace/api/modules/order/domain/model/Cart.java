package com.marketplace.api.modules.order.domain.model;

import com.marketplace.api.shared.domain.BaseEntity;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Cart aggregate root.
 *
 * <p>A buyer owns at most one {@link CartStatus#ACTIVE} cart at a time. Checked-out carts are
 * frozen as historical records and a fresh active cart is created on the next addition; a
 * partial unique index (see {@code db/migration}) enforces the single-active-cart rule in the
 * database for PostgreSQL deployments.</p>
 */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "carts")
public class Cart extends BaseEntity {

    /** Guards against a single line holding an absurd quantity in one checkout. */
    public static final int MAX_QUANTITY_PER_ITEM = 999;

    @Column(name = "buyer_id", nullable = false, updatable = false)
    private UUID buyerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CartStatus status = CartStatus.ACTIVE;

    @OneToMany(
        mappedBy = "cart",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    private List<CartItem> items = new ArrayList<>();

    public Cart(UUID buyerId) {        if (buyerId == null) {
            throw new DomainException("A cart requires a buyer");
        }
        this.buyerId = buyerId;
        this.status = CartStatus.ACTIVE;
    }

    /**
     * Adds a product to the cart, merging into the existing line when the product is already
     * present. The unit price is refreshed to the current catalog price so checkout always
     * charges a price the buyer could see.
     */
    public CartItem addItem(UUID productId, UUID sellerId, String productName, int quantity,
                            BigDecimal unitPrice, String currencyCode) {
        assertMutable();
        if (quantity <= 0) {
            throw new DomainException("Quantity must be greater than zero");
        }

        Optional<CartItem> existing = findItem(productId);
        if (existing.isPresent()) {
            CartItem item = existing.get();
            int newQuantity = item.getQuantity() + quantity;
            assertQuantityWithinLimit(newQuantity);
            item.setQuantity(newQuantity);
            item.setUnitPrice(unitPrice);
            item.setCurrencyCode(currencyCode);
            return item;
        }

        assertQuantityWithinLimit(quantity);
        CartItem item = new CartItem(this, productId, sellerId, productName, quantity, unitPrice, currencyCode);
        items.add(item);
        return item;
    }

    /** Sets the absolute quantity of a line; zero removes it. */
    public CartItem updateItemQuantity(UUID productId, int quantity) {
        assertMutable();
        if (quantity < 0) {
            throw new DomainException("Quantity cannot be negative");
        }

        CartItem item = findItem(productId)
            .orElseThrow(() -> new DomainException("Product is not present in the cart: " + productId));

        if (quantity == 0) {
            items.remove(item);
            return item;
        }

        assertQuantityWithinLimit(quantity);
        item.setQuantity(quantity);
        return item;
    }

    public void removeItem(UUID productId) {
        assertMutable();
        CartItem item = findItem(productId)
            .orElseThrow(() -> new DomainException("Product is not present in the cart: " + productId));
        items.remove(item);
    }

    public void clear() {
        assertMutable();
        items.clear();
    }

    /** Marks the cart as consumed by a successful checkout. */
    public void markCheckedOut() {
        assertMutable();
        if (items.isEmpty()) {
            throw new DomainException("Cannot check out an empty cart");
        }
        this.status = CartStatus.CHECKED_OUT;
    }

    public void abandon() {
        if (this.status == CartStatus.ACTIVE) {
            this.status = CartStatus.ABANDONED;
        }
    }

    public Optional<CartItem> findItem(UUID productId) {
        return items.stream()
            .filter(item -> item.getProductId().equals(productId))
            .findFirst();
    }

    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int getTotalItemCount() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }

    public BigDecimal getSubtotal() {
        return items.stream()
            .map(CartItem::lineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isOwnedBy(UUID buyerId) {
        return this.buyerId != null && this.buyerId.equals(buyerId);
    }

    public boolean isActive() {
        return this.status == CartStatus.ACTIVE;
    }

    private void assertMutable() {
        if (this.status != CartStatus.ACTIVE) {
            throw new DomainException("Cart is not active and can no longer be modified");
        }
    }

    private void assertQuantityWithinLimit(int quantity) {
        if (quantity > MAX_QUANTITY_PER_ITEM) {
            throw new DomainException("Quantity cannot exceed " + MAX_QUANTITY_PER_ITEM + " units per item");
        }
    }
}

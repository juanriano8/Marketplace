package com.marketplace.api.modules.order.domain.model;

import com.marketplace.api.shared.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CartTest {

    private static final UUID BUYER_ID = UUID.randomUUID();
    private static final UUID SELLER_ID = UUID.randomUUID();
    private static final UUID PRODUCT_ID = UUID.randomUUID();

    private Cart cart() {
        return new Cart(BUYER_ID);
    }

    private void addProduct(Cart cart, int quantity, String price) {
        cart.addItem(PRODUCT_ID, SELLER_ID, "Wireless Headphones", quantity, new BigDecimal(price), "USD");
    }

    @Test
    @DisplayName("adding the same product merges into a single line")
    void addingSameProductMergesLines() {
        Cart cart = cart();

        addProduct(cart, 2, "10.00");
        addProduct(cart, 3, "10.00");

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(5);
        assertThat(cart.getTotalItemCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("subtotal is the sum of every line")
    void subtotalIsSumOfLines() {
        Cart cart = cart();
        UUID otherProduct = UUID.randomUUID();

        addProduct(cart, 2, "10.50");
        cart.addItem(otherProduct, SELLER_ID, "Mouse", 1, new BigDecimal("5.25"), "USD");

        assertThat(cart.getSubtotal()).isEqualByComparingTo("26.25");
    }

    @Test
    @DisplayName("adding a non-positive quantity is rejected")
    void nonPositiveQuantityIsRejected() {
        Cart cart = cart();

        assertThatThrownBy(() -> addProduct(cart, 0, "10.00"))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("greater than zero");
    }

    @Test
    @DisplayName("quantity above the per-item ceiling is rejected")
    void quantityAboveCeilingIsRejected() {
        Cart cart = cart();

        assertThatThrownBy(() -> addProduct(cart, Cart.MAX_QUANTITY_PER_ITEM + 1, "10.00"))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("cannot exceed");
    }

    @Test
    @DisplayName("merged quantities that exceed the ceiling are rejected")
    void mergedQuantityAboveCeilingIsRejected() {
        Cart cart = cart();
        addProduct(cart, Cart.MAX_QUANTITY_PER_ITEM, "10.00");

        assertThatThrownBy(() -> addProduct(cart, 1, "10.00"))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("cannot exceed");
    }

    @Test
    @DisplayName("updating a line with quantity zero removes it")
    void zeroQuantityRemovesLine() {
        Cart cart = cart();
        addProduct(cart, 2, "10.00");

        cart.updateItemQuantity(PRODUCT_ID, 0);

        assertThat(cart.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("updating a line that is not in the cart is rejected")
    void updatingUnknownLineIsRejected() {
        Cart cart = cart();

        assertThatThrownBy(() -> cart.updateItemQuantity(PRODUCT_ID, 2))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("not present in the cart");
    }

    @Test
    @DisplayName("removing a line works and unknown products are rejected")
    void removeLine() {
        Cart cart = cart();
        addProduct(cart, 2, "10.00");

        cart.removeItem(PRODUCT_ID);

        assertThat(cart.isEmpty()).isTrue();
        assertThatThrownBy(() -> cart.removeItem(PRODUCT_ID))
            .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("a checked-out cart can no longer be modified")
    void checkedOutCartIsFrozen() {
        Cart cart = cart();
        addProduct(cart, 1, "10.00");

        cart.markCheckedOut();

        assertThat(cart.getStatus()).isEqualTo(CartStatus.CHECKED_OUT);
        assertThatThrownBy(() -> addProduct(cart, 1, "10.00"))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("not active");
        assertThatThrownBy(cart::clear)
            .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("an empty cart cannot be checked out")
    void emptyCartCannotBeCheckedOut() {
        Cart cart = cart();

        assertThatThrownBy(cart::markCheckedOut)
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("empty cart");
    }

    @Test
    @DisplayName("unit price is refreshed from the catalog on every mutation")
    void unitPriceIsRefreshed() {
        Cart cart = cart();
        addProduct(cart, 1, "10.00");

        addProduct(cart, 1, "12.00");

        assertThat(cart.getItems().get(0).getUnitPrice()).isEqualByComparingTo("12.00");
        assertThat(cart.getSubtotal()).isEqualByComparingTo("24.00");
    }

    @Test
    @DisplayName("ownership is scoped to the buyer")
    void ownershipIsScopedToBuyer() {
        Cart cart = cart();

        assertThat(cart.isOwnedBy(BUYER_ID)).isTrue();
        assertThat(cart.isOwnedBy(UUID.randomUUID())).isFalse();
        assertThat(cart.isActive()).isTrue();
    }

    @Nested
    @DisplayName("Line totals")
    class LineTotals {

        @Test
        @DisplayName("line total multiplies unit price by quantity with 2 decimals")
        void lineTotalIsRoundedToTwoDecimals() {
            Cart cart = cart();
            cart.addItem(PRODUCT_ID, SELLER_ID, "Cable", 3, new BigDecimal("3.333"), "USD");

            assertThat(cart.getItems().get(0).lineTotal()).isEqualByComparingTo("10.00");
        }
    }
}

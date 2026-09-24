package com.marketplace.api.modules.inventory.domain.model;

import com.marketplace.api.shared.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StockItemTest {

    private static final UUID PRODUCT_ID = UUID.randomUUID();
    private static final UUID SELLER_ID = UUID.randomUUID();

    private StockItem stock(int quantity) {
        return new StockItem(PRODUCT_ID, SELLER_ID, quantity);
    }

    @Nested
    @DisplayName("Reservation protects against overselling")
    class Reservation {

        @Test
        @DisplayName("reserving reduces the sellable quantity without touching the physical count")
        void reserveReducesSellableQuantity() {
            StockItem item = stock(10);

            item.reserve(4);

            assertThat(item.getAvailableQuantity()).isEqualTo(10);
            assertThat(item.getReservedQuantity()).isEqualTo(4);
            assertThat(item.getSellableQuantity()).isEqualTo(6);
        }

        @Test
        @DisplayName("two checkouts cannot reserve the same last unit twice")
        void concurrentReservationsCannotOversell() {
            StockItem item = stock(1);

            item.reserve(1);

            assertThatThrownBy(() -> item.reserve(1))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Insufficient stock");
            assertThat(item.getSellableQuantity()).isZero();
        }

        @Test
        @DisplayName("committing a reservation consumes physical stock")
        void commitConsumesStock() {
            StockItem item = stock(10);
            item.reserve(4);

            item.commitReserved(4);

            assertThat(item.getAvailableQuantity()).isEqualTo(6);
            assertThat(item.getReservedQuantity()).isZero();
            assertThat(item.getSellableQuantity()).isEqualTo(6);
        }

        @Test
        @DisplayName("releasing a reservation makes the units sellable again")
        void releaseRestoresSellableQuantity() {
            StockItem item = stock(10);
            item.reserve(4);

            item.releaseReserved(4);

            assertThat(item.getAvailableQuantity()).isEqualTo(10);
            assertThat(item.getReservedQuantity()).isZero();
            assertThat(item.getSellableQuantity()).isEqualTo(10);
        }

        @Test
        @DisplayName("cannot commit more units than were reserved")
        void cannotCommitUnreservedUnits() {
            StockItem item = stock(10);
            item.reserve(2);

            assertThatThrownBy(() -> item.commitReserved(3))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("only 2 are reserved");
        }

        @Test
        @DisplayName("releasing more units than reserved is clamped, never negative")
        void releaseIsClamped() {
            StockItem item = stock(10);
            item.reserve(2);

            item.releaseReserved(5);

            assertThat(item.getReservedQuantity()).isZero();
            assertThat(item.getSellableQuantity()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("Physical adjustments")
    class Adjustments {

        @Test
        @DisplayName("positive delta adds units")
        void positiveDeltaAdds() {
            StockItem item = stock(5);

            item.adjust(7);

            assertThat(item.getAvailableQuantity()).isEqualTo(12);
        }

        @Test
        @DisplayName("negative delta removes units")
        void negativeDeltaRemoves() {
            StockItem item = stock(5);

            item.adjust(-3);

            assertThat(item.getAvailableQuantity()).isEqualTo(2);
        }

        @Test
        @DisplayName("an adjustment cannot drive stock below zero")
        void cannotGoNegative() {
            StockItem item = stock(2);

            assertThatThrownBy(() -> item.adjust(-3))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("negative stock level");
            assertThat(item.getAvailableQuantity()).isEqualTo(2);
        }

        @Test
        @DisplayName("a zero delta is rejected as a no-op")
        void zeroDeltaIsRejected() {
            StockItem item = stock(2);

            assertThatThrownBy(() -> item.adjust(0))
                .isInstanceOf(DomainException.class);
        }

        @Test
        @DisplayName("absolute recount replaces the physical quantity")
        void absoluteRecount() {
            StockItem item = stock(50);

            item.setPhysicalQuantity(12);

            assertThat(item.getAvailableQuantity()).isEqualTo(12);
        }

        @Test
        @DisplayName("low stock flag follows the threshold")
        void lowStockFlag() {
            StockItem item = stock(50);
            assertThat(item.isLowStock()).isFalse();

            item.updateLowStockThreshold(60);
            assertThat(item.isLowStock()).isTrue();
        }
    }

    @Test
    @DisplayName("negative initial stock is rejected")
    void negativeInitialStockIsRejected() {
        assertThatThrownBy(() -> stock(-1))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("cannot be negative");
    }

    @Test
    @DisplayName("ownership check is scoped to the seller")
    void ownershipCheck() {
        StockItem item = stock(1);

        assertThat(item.isOwnedBy(SELLER_ID)).isTrue();
        assertThat(item.isOwnedBy(UUID.randomUUID())).isFalse();
    }
}

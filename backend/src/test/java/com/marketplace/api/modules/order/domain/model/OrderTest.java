package com.marketplace.api.modules.order.domain.model;

import com.marketplace.api.shared.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    private static final UUID BUYER_ID = UUID.randomUUID();
    private static final UUID SELLER_A = UUID.randomUUID();
    private static final UUID SELLER_B = UUID.randomUUID();

    private Order orderWithTwoSellers() {
        Order order = new Order(BUYER_ID, "USD");

        SubOrder first = order.addSubOrder(SELLER_A);
        first.addItem(UUID.randomUUID(), "Headphones", 2, new BigDecimal("50.00"), "USD");

        SubOrder second = order.addSubOrder(SELLER_B);
        second.addItem(UUID.randomUUID(), "Mouse", 1, new BigDecimal("25.00"), "USD");

        order.recalculateTotal();
        return order;
    }

    @Test
    @DisplayName("an order groups lines into one sub-order per seller")
    void orderIsSplitPerSeller() {
        Order order = orderWithTwoSellers();

        assertThat(order.getSubOrders()).hasSize(2);
        assertThat(order.getSubOrders()).extracting(SubOrder::getSellerId)
            .containsExactlyInAnyOrder(SELLER_A, SELLER_B);
    }

    @Test
    @DisplayName("the order total is the sum of every sub-order subtotal")
    void totalIsSumOfSubOrderSubtotals() {
        Order order = orderWithTwoSellers();

        assertThat(order.getTotalAmount()).isEqualByComparingTo("125.00");
        assertThat(order.totalAsMoney().amount()).isEqualByComparingTo("125.00");
        assertThat(order.totalAsMoney().currency().getCurrencyCode()).isEqualTo("USD");
    }

    @Test
    @DisplayName("a sub-order subtotal sums its own lines only")
    void subOrderSubtotalIsScoped() {
        Order order = orderWithTwoSellers();

        assertThat(order.getSubOrders())
            .allSatisfy(subOrder -> assertThat(subOrder.getSubtotal()).isPositive());
        assertThat(order.getSubOrders().stream()
            .map(SubOrder::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add))
            .isEqualByComparingTo("125.00");
    }

    @Test
    @DisplayName("a newly created order starts pending payment")
    void newOrderIsPendingPayment() {
        assertThat(new Order(BUYER_ID, "USD").getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
    }

    @Test
    @DisplayName("order number is generated and unique per order")
    void orderNumberIsGenerated() {
        Order first = new Order(BUYER_ID, "USD");
        Order second = new Order(BUYER_ID, "USD");

        assertThat(first.getOrderNumber()).startsWith("ORD-");
        assertThat(first.getOrderNumber()).isNotEqualTo(second.getOrderNumber());
    }

    @Test
    @DisplayName("paying an order moves it and its sub-orders forward")
    void payingMovesSubOrdersToProcessing() {
        Order order = orderWithTwoSellers();

        order.markPaid("pay-123");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getPaymentReference()).isEqualTo("pay-123");
        assertThat(order.getPaidAt()).isNotNull();
        assertThat(order.getSubOrders()).allSatisfy(sub ->
            assertThat(sub.getStatus()).isEqualTo(SubOrderStatus.PROCESSING)
        );
    }

    @Test
    @DisplayName("a failed payment cannot be followed by a successful one on the same order")
    void paymentFailureIsTerminalForTheAttempt() {
        Order order = orderWithTwoSellers();
        order.markPaymentFailed("card declined");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_FAILED);
        assertThatThrownBy(() -> order.markPaid("pay-999"))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("pending payment");
    }

    @Test
    @DisplayName("an order is only completed once every seller delivered")
    void orderCompletesWhenFullyDelivered() {
        Order order = orderWithTwoSellers();
        order.markPaid("pay-1");

        SubOrder first = order.getSubOrders().get(0);
        SubOrder second = order.getSubOrders().get(1);

        first.ship("TRK-1", "DHL");
        first.markDelivered();
        order.completeIfFullyDelivered();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);

        second.ship("TRK-2", "DHL");
        second.markDelivered();
        order.completeIfFullyDelivered();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    @DisplayName("cancelling an order cancels every sub-order and is idempotent")
    void cancellingCascades() {
        Order order = orderWithTwoSellers();

        order.cancel();
        order.cancel();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getSubOrders()).allSatisfy(sub ->
            assertThat(sub.getStatus()).isEqualTo(SubOrderStatus.CANCELLED)
        );
    }

    @Test
    @DisplayName("a completed order cannot be cancelled")
    void completedOrderCannotBeCancelled() {
        Order order = orderWithTwoSellers();
        order.markPaid("pay-1");
        order.getSubOrders().forEach(sub -> {
            sub.ship("TRK", "DHL");
            sub.markDelivered();
        });
        order.completeIfFullyDelivered();

        assertThatThrownBy(order::cancel)
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("Completed orders");
    }

    @Test
    @DisplayName("an order with no lines cannot be totalled")
    void emptyOrderCannotBeTotalled() {
        Order order = new Order(BUYER_ID, "USD");

        assertThatThrownBy(order::recalculateTotal)
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("greater than zero");
    }

    @Test
    @DisplayName("seller and buyer scoping helpers behave as expected")
    void ownershipHelpers() {
        Order order = orderWithTwoSellers();

        assertThat(order.isOwnedBy(BUYER_ID)).isTrue();
        assertThat(order.isOwnedBy(UUID.randomUUID())).isFalse();
        assertThat(order.hasSeller(SELLER_A)).isTrue();
        assertThat(order.hasSeller(UUID.randomUUID())).isFalse();
    }

    @Nested
    @DisplayName("Sub-order state machine")
    class SubOrderStateMachine {

        @Test
        @DisplayName("a sub-order cannot ship before payment")
        void cannotShipBeforePayment() {
            Order order = new Order(BUYER_ID, "USD");
            SubOrder subOrder = order.addSubOrder(SELLER_A);

            assertThatThrownBy(() -> subOrder.ship("TRK-1", "DHL"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("PROCESSING");
        }

        @Test
        @DisplayName("shipping requires a tracking number")
        void shippingRequiresTrackingNumber() {
            Order order = new Order(BUYER_ID, "USD");
            SubOrder subOrder = order.addSubOrder(SELLER_A);
            subOrder.markProcessing();

            assertThatThrownBy(() -> subOrder.ship("   ", "DHL"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("tracking number");
        }

        @Test
        @DisplayName("shipping records the dispatch details")
        void shippingRecordsDetails() {
            Order order = new Order(BUYER_ID, "USD");
            SubOrder subOrder = order.addSubOrder(SELLER_A);
            subOrder.markProcessing();

            subOrder.ship("TRK-42", "DHL");

            assertThat(subOrder.getStatus()).isEqualTo(SubOrderStatus.SHIPPED);
            assertThat(subOrder.getTrackingNumber()).isEqualTo("TRK-42");
            assertThat(subOrder.getCarrier()).isEqualTo("DHL");
            assertThat(subOrder.getShippedAt()).isNotNull();
            assertThat(subOrder.isShipped()).isTrue();
        }

        @Test
        @DisplayName("only a shipped sub-order can be delivered")
        void onlyShippedCanBeDelivered() {
            Order order = new Order(BUYER_ID, "USD");
            SubOrder subOrder = order.addSubOrder(SELLER_A);

            assertThatThrownBy(subOrder::markDelivered)
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("shipped");
        }

        @Test
        @DisplayName("a delivered sub-order cannot be cancelled")
        void deliveredCannotBeCancelled() {
            Order order = new Order(BUYER_ID, "USD");
            SubOrder subOrder = order.addSubOrder(SELLER_A);
            subOrder.markProcessing();
            subOrder.ship("TRK", "DHL");
            subOrder.markDelivered();

            assertThatThrownBy(subOrder::cancel)
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("cannot be cancelled");
        }

        @Test
        @DisplayName("a sub-order rejects lines in a different currency")
        void mixedCurrencyIsRejected() {
            Order order = new Order(BUYER_ID, "USD");
            SubOrder subOrder = order.addSubOrder(SELLER_A);

            assertThatThrownBy(() ->
                subOrder.addItem(UUID.randomUUID(), "Item", 1, new BigDecimal("10.00"), "EUR")
            )
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Cannot mix currencies");
        }

        @Test
        @DisplayName("a sub-order rejects non-positive quantities")
        void nonPositiveQuantityIsRejected() {
            Order order = new Order(BUYER_ID, "USD");
            SubOrder subOrder = order.addSubOrder(SELLER_A);

            assertThatThrownBy(() ->
                subOrder.addItem(UUID.randomUUID(), "Item", 0, new BigDecimal("10.00"), "USD")
            )
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("greater than zero");
        }

        @Test
        @DisplayName("containsProduct only matches its own lines")
        void containsProductIsScoped() {
            Order order = new Order(BUYER_ID, "USD");
            SubOrder subOrder = order.addSubOrder(SELLER_A);
            UUID productId = UUID.randomUUID();
            subOrder.addItem(productId, "Item", 1, new BigDecimal("10.00"), "USD");

            assertThat(subOrder.containsProduct(productId)).isTrue();
            assertThat(subOrder.containsProduct(UUID.randomUUID())).isFalse();
            assertThat(subOrder.isOwnedBy(SELLER_A)).isTrue();
            assertThat(subOrder.isOwnedBy(SELLER_B)).isFalse();
        }
    }
}

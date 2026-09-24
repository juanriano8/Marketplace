package com.marketplace.api.modules.order.application.service;

import com.marketplace.api.modules.inventory.domain.model.StockReservation;
import com.marketplace.api.modules.inventory.domain.port.inbound.StockQueryPort;
import com.marketplace.api.modules.order.application.dto.CheckoutResponse;
import com.marketplace.api.modules.order.application.mapper.OrderMapper;
import com.marketplace.api.modules.order.domain.model.Cart;
import com.marketplace.api.modules.order.domain.model.CartItem;
import com.marketplace.api.modules.order.domain.model.Order;
import com.marketplace.api.modules.order.domain.model.SubOrder;
import com.marketplace.api.modules.order.domain.port.inbound.CheckoutUseCase;
import com.marketplace.api.modules.order.domain.port.outbound.CartRepositoryPort;
import com.marketplace.api.modules.order.domain.port.outbound.OrderRepositoryPort;
import com.marketplace.api.modules.payment.domain.model.PaymentResult;
import com.marketplace.api.modules.payment.domain.port.outbound.PaymentGatewayPort;
import com.marketplace.api.modules.product.domain.model.Product;
import com.marketplace.api.modules.product.domain.port.outbound.ProductRepositoryPort;
import com.marketplace.api.modules.user.domain.model.User;
import com.marketplace.api.modules.user.domain.port.outbound.UserRepositoryPort;
import com.marketplace.api.shared.exception.DomainException;
import com.marketplace.api.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Turns a buyer's cart into a paid (or failed) order.
 *
 * <p>Consistency strategy: every stock reservation happens inside this transaction before the
 * gateway is called, and is then either committed or released according to the gateway outcome.
 * That way the marketplace never sells the same unit twice even if two buyers check out
 * simultaneously — the loser fails on optimistic locking / insufficient stock rather than
 * overselling.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckoutService implements CheckoutUseCase {

    private final CartRepositoryPort cartRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final StockQueryPort stockQueryPort;
    private final PaymentGatewayPort paymentGatewayPort;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public CheckoutResponse checkout(UUID buyerId, String notes) {
        Cart cart = cartRepositoryPort.findActiveByBuyerId(buyerId)
            .orElseThrow(() -> new ResourceNotFoundException("No active cart found for the authenticated buyer"));

        if (cart.isEmpty()) {
            throw new DomainException("Cannot check out an empty cart");
        }

        String buyerEmail = userRepositoryPort.findById(buyerId)
            .map(User::getEmail)
            .orElseThrow(() -> new ResourceNotFoundException("Buyer not found with id: " + buyerId));

        List<CartItem> cartItems = cart.getItems();

        // 1. Re-validate against the live catalog: prices, status and single-currency invariant.
        Map<UUID, Product> products = loadAndValidateProducts(cartItems);
        String currencyCode = resolveCurrency(products.values());

        // 2. Build the order aggregate, one sub-order per seller.
        Order order = new Order(buyerId, currencyCode);
        Map<UUID, SubOrder> subOrdersBySeller = new LinkedHashMap<>();

        for (CartItem cartItem : cartItems) {
            Product product = products.get(cartItem.getProductId());

            SubOrder subOrder = subOrdersBySeller.computeIfAbsent(
                product.getSellerId(),
                order::addSubOrder
            );

            subOrder.addItem(
                product.getId(),
                product.getName(),
                cartItem.getQuantity(),
                product.getPrice(),
                product.getCurrencyCode()
            );
        }

        order.recalculateTotal();
        Order savedOrder = orderRepositoryPort.save(order);

        // 3. Reserve stock for every line before touching the payment gateway.
        List<StockReservation> reservations = reserveStock(savedOrder, products, cartItems);

        // 4. Charge.
        PaymentResult payment = paymentGatewayPort.initiatePayment(
            savedOrder.getId(),
            savedOrder.totalAsMoney(),
            buyerEmail
        );

        if (!payment.isSuccessful()) {
            releaseReservations(reservations, savedOrder.getOrderNumber());
            savedOrder.markPaymentFailed(payment.message());
            orderRepositoryPort.save(savedOrder);

            log.warn("Checkout failed for buyer {} order {}: {}", buyerId, savedOrder.getOrderNumber(), payment.message());

            Order failedOrder = orderRepositoryPort.requireByIdWithDetails(savedOrder.getId());

            return new CheckoutResponse(
                orderMapper.toResponse(failedOrder),
                false,
                payment.reference(),
                failedOrder.getTotalAmount(),
                payment.redirectUrl(),
                payment.message()
            );
        }

        // 5. Payment captured: consume reservations, close the cart and mark the order paid.
        reservations.forEach(reservation ->
            stockQueryPort.commitReservation(reservation, savedOrder.getOrderNumber())
        );

        savedOrder.markPaid(payment.reference());
        orderRepositoryPort.save(savedOrder);

        cart.markCheckedOut();
        cartRepositoryPort.save(cart);

        // Force the writes out now: once the money is captured the marketplace must be able to tell
        // apart "this failed before charging" from "this failed after charging".
        orderRepositoryPort.flush();

        Order paidOrder;
        try {
            paidOrder = orderRepositoryPort.requireByIdWithDetails(savedOrder.getId());
        } catch (RuntimeException ex) {
            // The buyer has been charged but the order could not be persisted: refund and fail hard
            // so the caller never sees a success response for an order that does not exist.
            compensateCapturedPayment(payment, savedOrder.getOrderNumber(), ex);
            throw ex;
        }

        log.info("Checkout completed for buyer {}: order {} total {} {}",
            buyerId, paidOrder.getOrderNumber(), paidOrder.getTotalAmount(), paidOrder.getCurrencyCode());

        String message = notes != null && !notes.isBlank()
            ? payment.message() + " | Buyer note: " + notes.trim()
            : payment.message();

        return new CheckoutResponse(
            orderMapper.toResponse(paidOrder),
            true,
            payment.reference(),
            paidOrder.getTotalAmount(),
            payment.redirectUrl(),
            message
        );
    }

    private Map<UUID, Product> loadAndValidateProducts(List<CartItem> cartItems) {
        Map<UUID, Product> products = new LinkedHashMap<>();

        for (CartItem cartItem : cartItems) {
            Product product = productRepositoryPort.findById(cartItem.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Product no longer exists: " + cartItem.getProductId()
                ));

            if (!product.isActive()) {
                throw new DomainException(
                    "Product '" + product.getName() + "' is no longer available for purchase"
                );
            }
            if (product.getSellerId() == null) {
                throw new DomainException("Product '" + product.getName() + "' has no assigned seller");
            }

            products.put(product.getId(), product);
        }

        return products;
    }

    private String resolveCurrency(Iterable<Product> products) {
        String currency = null;
        for (Product product : products) {
            if (currency == null) {
                currency = product.getCurrencyCode();
            } else if (!currency.equals(product.getCurrencyCode())) {
                throw new DomainException(
                    "All products in a single order must share the same currency; found "
                        + currency + " and " + product.getCurrencyCode()
                );
            }
        }
        return currency != null ? currency : "USD";
    }

    private List<StockReservation> reserveStock(Order order, Map<UUID, Product> products, List<CartItem> cartItems) {
        List<StockReservation> reservations = new ArrayList<>();

        try {
            for (CartItem cartItem : cartItems) {
                Product product = products.get(cartItem.getProductId());
                stockQueryPort.reserveStock(
                    product.getId(),
                    product.getSellerId(),
                    cartItem.getQuantity(),
                    order.getOrderNumber()
                );
                reservations.add(new StockReservation(product.getId(), product.getSellerId(), cartItem.getQuantity()));
            }
        } catch (RuntimeException ex) {
            // Roll back the reservations already taken so a partial checkout does not lock stock.
            releaseReservations(reservations, order.getOrderNumber());
            throw ex;
        }

        return reservations;
    }

    private void releaseReservations(List<StockReservation> reservations, String reference) {
        for (StockReservation reservation : reservations) {
            try {
                stockQueryPort.releaseReservation(reservation, reference);
            } catch (RuntimeException ex) {
                log.error("Could not release reservation for product {} of order {}: {}",
                    reservation.productId(), reference, ex.getMessage());
            }
        }
    }

    /**
     * Best-effort refund used when the payment was captured but the order could not be completed.
     * A failure here is logged loudly because it requires manual reconciliation.
     */
    private void compensateCapturedPayment(PaymentResult payment, String orderNumber, RuntimeException cause) {
        log.error("Order {} failed after the payment was captured; issuing a refund", orderNumber, cause);
        try {
            paymentGatewayPort.refund(payment.reference(), payment.amount());
        } catch (RuntimeException refundFailure) {
            log.error("REFUND FAILED for payment {} of order {} — manual reconciliation required",
                payment.reference(), orderNumber, refundFailure);
        }
    }
}

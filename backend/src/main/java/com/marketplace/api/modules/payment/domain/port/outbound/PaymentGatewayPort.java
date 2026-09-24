package com.marketplace.api.modules.payment.domain.port.outbound;

import com.marketplace.api.modules.payment.domain.model.PaymentResult;
import com.marketplace.api.shared.domain.Money;

import java.util.UUID;

/**
 * Outbound port describing the payment gateway the marketplace depends on.
 *
 * <p>Keeping this as a port lets the order module start a payment without knowing which
 * provider is wired in, and makes the provider swappable (and testable) in isolation.</p>
 */
public interface PaymentGatewayPort {

    /**
     * Starts a payment for the given order.
     *
     * @param orderId   marketplace order identifier used as the idempotency key
     * @param amount    amount to charge
     * @param buyerEmail email of the buyer, forwarded to the provider for receipts
     * @return the gateway outcome, never {@code null}
     */
    PaymentResult initiatePayment(UUID orderId, Money amount, String buyerEmail);

    /**
     * Reverses a previously captured payment. Used when an order is cancelled after payment.
     */
    PaymentResult refund(String paymentReference, Money amount);
}

package com.marketplace.api.modules.payment.infrastructure.adapter.outbound.gateway;

import com.marketplace.api.modules.payment.domain.model.PaymentResult;
import com.marketplace.api.modules.payment.domain.model.PaymentStatus;
import com.marketplace.api.modules.payment.domain.port.outbound.PaymentGatewayPort;
import com.marketplace.api.shared.domain.Money;
import com.marketplace.api.shared.exception.DomainException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Development/stub implementation of {@link PaymentGatewayPort}.
 *
 * <p>It is deterministic so that checkout and order-status flows can be exercised without an
 * external provider: any positive amount is captured and receives a generated reference.
 * Replace this adapter with the real provider client when credentials are available; no
 * caller needs to change because it lives behind the port.</p>
 */
@Slf4j
@Component
public class StubPaymentGatewayAdapter implements PaymentGatewayPort {

    private final boolean alwaysCapture;

    public StubPaymentGatewayAdapter(
        @Value("${app.payment.stub.always-capture:true}") boolean alwaysCapture
    ) {
        this.alwaysCapture = alwaysCapture;
    }

    @Override
    public PaymentResult initiatePayment(UUID orderId, Money amount, String buyerEmail) {
        if (amount == null || !amount.isPositiveOrZero() || amount.amount().signum() <= 0) {
            throw new DomainException("Cannot charge a non-positive amount");
        }

        String reference = "stub-" + orderId + "-" + UUID.randomUUID().toString().substring(0, 8);

        if (!alwaysCapture) {
            log.info("[payment-stub] Payment left pending for order {} ({} {})",
                orderId, amount.amount(), amount.currency().getCurrencyCode());
            return PaymentResult.pending(reference, amount, null);
        }

        log.info("[payment-stub] Captured {} {} for order {} (buyer={})",
            amount.amount(), amount.currency().getCurrencyCode(), orderId, buyerEmail);

        return new PaymentResult(
            reference,
            PaymentStatus.CAPTURED,
            amount,
            null,
            "Captured by stub gateway"
        );
    }

    @Override
    public PaymentResult refund(String paymentReference, Money amount) {
        if (paymentReference == null || paymentReference.isBlank()) {
            throw new DomainException("A payment reference is required to issue a refund");
        }
        log.info("[payment-stub] Refunded {} {} for payment {}",
            amount.amount(), amount.currency().getCurrencyCode(), paymentReference);

        return new PaymentResult(
            paymentReference,
            PaymentStatus.REFUNDED,
            amount,
            null,
            "Refunded by stub gateway"
        );
    }
}

package com.marketplace.api.modules.payment.domain.model;

import com.marketplace.api.shared.domain.Money;

import java.util.UUID;

/**
 * Value object describing the outcome of an external payment operation.
 *
 * @param reference   gateway-side payment identifier
 * @param status      resulting payment status
 * @param amount      amount that was processed
 * @param redirectUrl URL the client should be redirected to in order to complete the payment (may be null)
 * @param message     human readable detail (may be null)
 */
public record PaymentResult(
    String reference,
    PaymentStatus status,
    Money amount,
    String redirectUrl,
    String message
) {

    public static PaymentResult captured(String reference, Money amount) {
        return new PaymentResult(reference, PaymentStatus.CAPTURED, amount, null, "Payment captured");
    }

    public static PaymentResult pending(String reference, Money amount, String redirectUrl) {
        return new PaymentResult(reference, PaymentStatus.PENDING, amount, redirectUrl, "Awaiting confirmation");
    }

    public static PaymentResult failed(Money amount, String message) {
        return new PaymentResult(null, PaymentStatus.FAILED, amount, null, message);
    }

    public boolean isSuccessful() {
        return status == PaymentStatus.CAPTURED || status == PaymentStatus.AUTHORIZED;
    }

    public UUID referenceAsUuid() {
        if (reference == null) {
            return null;
        }
        try {
            return UUID.fromString(reference);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}

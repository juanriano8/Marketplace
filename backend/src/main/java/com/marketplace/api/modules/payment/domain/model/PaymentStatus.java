package com.marketplace.api.modules.payment.domain.model;

/**
 * Lifecycle of a payment attempt issued against the external payment gateway.
 */
public enum PaymentStatus {
    PENDING,
    AUTHORIZED,
    CAPTURED,
    FAILED,
    REFUNDED
}

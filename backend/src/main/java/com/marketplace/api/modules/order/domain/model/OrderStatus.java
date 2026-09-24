package com.marketplace.api.modules.order.domain.model;

public enum OrderStatus {
    PENDING_PAYMENT,
    PAID,
    PAYMENT_FAILED,
    CANCELLED,
    COMPLETED
}

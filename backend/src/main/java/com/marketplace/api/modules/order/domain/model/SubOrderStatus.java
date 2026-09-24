package com.marketplace.api.modules.order.domain.model;

/**
 * Fulfilment status of a seller-scoped portion of a buyer's order.
 */
public enum SubOrderStatus {
    PENDING,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED
}

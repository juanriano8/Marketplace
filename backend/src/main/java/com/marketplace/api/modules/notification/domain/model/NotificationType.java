package com.marketplace.api.modules.notification.domain.model;

/** Tipos de notificación que genera el marketplace. */
public enum NotificationType {
    SELLER_VERIFIED,
    SELLER_REJECTED,
    PRODUCT_APPROVED,
    PRODUCT_REJECTED,
    ORDER_SHIPPED,
    ORDER_DELIVERED,
    REVIEW_RECEIVED
}

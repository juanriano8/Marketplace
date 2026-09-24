package com.marketplace.api.modules.inventory.domain.model;

/**
 * Reason behind a stock mutation, kept for the administrative audit trail.
 */
public enum StockMovementType {
    /** Initial ledger entry created together with the product. */
    INITIAL,
    /** Seller-driven physical count adjustment (positive or negative). */
    ADJUSTMENT,
    /** Units held for a checkout that is still awaiting payment confirmation. */
    RESERVATION,
    /** Stock committed by a paid order. */
    SALE,
    /** Stock returned or released after a cancellation or return. */
    CANCELLATION,
    /** Correction performed by an administrator. */
    ADMIN_CORRECTION
}

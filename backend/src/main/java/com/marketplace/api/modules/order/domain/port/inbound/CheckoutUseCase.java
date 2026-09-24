package com.marketplace.api.modules.order.domain.port.inbound;

import com.marketplace.api.modules.order.application.dto.CheckoutResponse;

import java.util.UUID;

public interface CheckoutUseCase {

    /**
     * Converts the buyer's active cart into an order, splits it per seller, validates stock and
     * starts the payment.
     */
    CheckoutResponse checkout(UUID buyerId, String notes);
}

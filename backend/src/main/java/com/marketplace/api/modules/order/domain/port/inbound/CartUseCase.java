package com.marketplace.api.modules.order.domain.port.inbound;

import com.marketplace.api.modules.order.application.dto.CartResponse;

import java.util.UUID;

public interface CartUseCase {

    /** Returns the buyer's active cart, creating an empty one when none exists yet. */
    CartResponse getActiveCart(UUID buyerId);

    CartResponse addItem(UUID buyerId, UUID productId, int quantity);

    CartResponse updateItemQuantity(UUID buyerId, UUID productId, int quantity);

    CartResponse removeItem(UUID buyerId, UUID productId);

    CartResponse clearCart(UUID buyerId);
}

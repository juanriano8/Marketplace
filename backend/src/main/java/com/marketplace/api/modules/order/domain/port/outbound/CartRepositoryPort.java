package com.marketplace.api.modules.order.domain.port.outbound;

import com.marketplace.api.modules.order.domain.model.Cart;

import java.util.Optional;
import java.util.UUID;

public interface CartRepositoryPort {

    Cart save(Cart cart);

    Optional<Cart> findById(UUID id);

    /** Returns the buyer's single mutable cart, if one exists. */
    Optional<Cart> findActiveByBuyerId(UUID buyerId);
}

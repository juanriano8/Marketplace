package com.marketplace.api.modules.order.infrastructure.adapter.outbound.persistence;

import com.marketplace.api.modules.order.domain.model.Cart;
import com.marketplace.api.modules.order.domain.model.CartStatus;
import com.marketplace.api.modules.order.domain.port.outbound.CartRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CartPersistenceAdapter implements CartRepositoryPort {

    private final SpringDataCartRepository springDataCartRepository;

    @Override
    public Cart save(Cart cart) {
        return springDataCartRepository.save(cart);
    }

    @Override
    public Optional<Cart> findById(UUID id) {
        Optional<Cart> found = springDataCartRepository.findById(id);
        // Initialise the lazy lines while the session is open.
        found.ifPresent(cart -> cart.getItems().size());
        return found;
    }

    @Override
    public Optional<Cart> findActiveByBuyerId(UUID buyerId) {
        Optional<Cart> found = springDataCartRepository
            .findFirstByBuyerIdAndStatusOrderByCreatedAtDesc(buyerId, CartStatus.ACTIVE);
        found.ifPresent(cart -> cart.getItems().size());
        return found;
    }
}

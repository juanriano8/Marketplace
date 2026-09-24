package com.marketplace.api.modules.order.infrastructure.adapter.outbound.persistence;

import com.marketplace.api.modules.order.domain.model.Cart;
import com.marketplace.api.modules.order.domain.model.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataCartRepository extends JpaRepository<Cart, UUID> {

    Optional<Cart> findFirstByBuyerIdAndStatusOrderByCreatedAtDesc(UUID buyerId, CartStatus status);
}

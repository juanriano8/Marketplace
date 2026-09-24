package com.marketplace.api.modules.order.infrastructure.adapter.outbound.persistence;

import com.marketplace.api.modules.order.domain.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataOrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * Eagerly loads sub-orders so a page of orders can be mapped to DTOs; the sub-order lines are
     * then loaded in batches through {@code @BatchSize} on the collection.
     */
    @EntityGraph(attributePaths = {"subOrders"})
    Page<Order> findByBuyerId(UUID buyerId, Pageable pageable);
}

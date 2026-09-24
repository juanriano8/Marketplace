package com.marketplace.api.modules.order.domain.port.outbound;

import com.marketplace.api.modules.order.domain.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepositoryPort {

    Order save(Order order);

    /**
     * Forces pending changes to the database so failures surface at a known point in the checkout
     * sequence instead of at commit time.
     */
    void flush();

    Optional<Order> findById(UUID id);

    /**
     * Loads the whole aggregate (sub-orders and their lines) so it can be mapped to a DTO.
     *
     * @throws com.marketplace.api.shared.exception.ResourceNotFoundException when the order does not exist
     */
    Order requireByIdWithDetails(UUID id);

    Optional<Order> findByOrderNumber(String orderNumber);

    /** Buyer purchase history, newest first (ordering supplied by the caller via {@link Pageable}). */
    Page<Order> findByBuyerId(UUID buyerId, Pageable pageable);

    /** Global transaction view for administrators. */
    Page<Order> findAll(Pageable pageable);
}

package com.marketplace.api.modules.order.infrastructure.adapter.outbound.persistence;

import com.marketplace.api.modules.order.domain.model.Order;
import com.marketplace.api.modules.order.domain.model.SubOrder;
import com.marketplace.api.modules.order.domain.port.outbound.OrderRepositoryPort;
import com.marketplace.api.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderPersistenceAdapter implements OrderRepositoryPort {

    private final SpringDataOrderRepository springDataOrderRepository;

    @Override
    public Order save(Order order) {
        return springDataOrderRepository.save(order);
    }

    @Override
    public void flush() {
        springDataOrderRepository.flush();
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return springDataOrderRepository.findById(id);
    }

    @Override
    public Order requireByIdWithDetails(UUID id) {
        Optional<Order> found = springDataOrderRepository.findById(id);

        // Touch the lazy sub-order collections while the session is still open so callers can map
        // the full aggregate to a DTO afterwards.
        Order order = found.orElseThrow(
            () -> new ResourceNotFoundException("Order not found with id: " + id)
        );
        for (SubOrder subOrder : order.getSubOrders()) {
            subOrder.getItems().size();
        }

        return order;
    }

    @Override
    public Optional<Order> findByOrderNumber(String orderNumber) {
        return springDataOrderRepository.findByOrderNumber(orderNumber);
    }

    @Override
    public Page<Order> findByBuyerId(UUID buyerId, Pageable pageable) {
        return springDataOrderRepository.findByBuyerId(buyerId, pageable);
    }

    @Override
    public Page<Order> findAll(Pageable pageable) {
        return springDataOrderRepository.findAll(pageable);
    }
}

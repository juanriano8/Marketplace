package com.marketplace.api.modules.order.application.service;

import com.marketplace.api.modules.order.application.dto.OrderResponse;
import com.marketplace.api.modules.order.application.mapper.OrderMapper;
import com.marketplace.api.modules.order.domain.model.Order;
import com.marketplace.api.modules.order.domain.model.SubOrder;
import com.marketplace.api.modules.order.domain.model.SubOrderStatus;
import com.marketplace.api.modules.order.domain.port.inbound.OrderQueryUseCase;
import com.marketplace.api.modules.order.domain.port.outbound.OrderRepositoryPort;
import com.marketplace.api.modules.order.domain.port.outbound.SubOrderRepositoryPort;
import com.marketplace.api.shared.exception.DomainException;
import com.marketplace.api.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderQueryService implements OrderQueryUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final SubOrderRepositoryPort subOrderRepositoryPort;
    private final OrderMapper orderMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> listBuyerOrders(UUID buyerId, Pageable pageable) {
        return orderRepositoryPort.findByBuyerId(buyerId, pageable)
            .map(orderMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getBuyerOrder(UUID buyerId, UUID orderId) {
        Order order = orderRepositoryPort.requireByIdWithDetails(orderId);

        // BOLA check — a buyer may only read their own orders.
        if (!order.isOwnedBy(buyerId)) {
            throw new ResourceNotFoundException("Order not found with id: " + orderId);
        }

        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse.SubOrderResponse> listSellerSubOrders(UUID sellerId, SubOrderStatus status,
                                                                    Pageable pageable) {
        Page<SubOrder> page = status == null
            ? subOrderRepositoryPort.findBySellerId(sellerId, pageable)
            : subOrderRepositoryPort.findBySellerIdAndStatus(sellerId, status, pageable);

        return page.map(orderMapper::toResponse);
    }

    @Override
    @Transactional
    public OrderResponse.SubOrderResponse shipSubOrder(UUID sellerId, UUID subOrderId,
                                                       String trackingNumber, String carrier) {
        SubOrder subOrder = subOrderRepositoryPort.findByIdWithOrder(subOrderId)
            .orElseThrow(() -> new ResourceNotFoundException("Sub-order not found with id: " + subOrderId));

        // BOLA check — a seller may only dispatch their own portion of an order.
        if (!subOrder.isOwnedBy(sellerId)) {
            throw new DomainException("You are not authorized to update this sub-order");
        }

        subOrder.ship(trackingNumber, carrier);
        SubOrder saved = subOrderRepositoryPort.save(subOrder);

        // Close the parent order when every seller has delivered.
        Order order = saved.getOrder();
        if (order != null) {
            order.completeIfFullyDelivered();
            orderRepositoryPort.save(order);
        }

        log.info("Seller {} shipped sub-order {} with tracking {}", sellerId, subOrderId, trackingNumber);
        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> listAllOrders(Pageable pageable) {
        return orderRepositoryPort.findAll(pageable)
            .map(orderMapper::toResponse);
    }
}

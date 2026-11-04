package com.marketplace.api.modules.order.domain.port.inbound;

import com.marketplace.api.modules.order.application.dto.OrderResponse;
import com.marketplace.api.modules.order.domain.model.SubOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OrderQueryUseCase {

    /** Buyer purchase history. */
    Page<OrderResponse> listBuyerOrders(UUID buyerId, Pageable pageable);

    OrderResponse getBuyerOrder(UUID buyerId, UUID orderId);

    /** Seller dispatch queue; {@code status} may be null to list every sub-order of the seller. */
    Page<OrderResponse.SubOrderResponse> listSellerSubOrders(UUID sellerId, SubOrderStatus status, Pageable pageable);

    OrderResponse.SubOrderResponse shipSubOrder(UUID sellerId, UUID subOrderId, String trackingNumber, String carrier);

    /**
     * Marks a shipped sub-order as delivered. When every sub-order of the parent order is
     * delivered, the order transitions to COMPLETED.
     */
    OrderResponse.SubOrderResponse markSubOrderDelivered(UUID sellerId, UUID subOrderId);

    /** Global transaction view for administrators, optionally filtered by status. */
    Page<OrderResponse> listAllOrders(Pageable pageable);
}

package com.marketplace.api.modules.order.domain.port.outbound;

import com.marketplace.api.modules.order.domain.model.SubOrder;
import com.marketplace.api.modules.order.domain.model.SubOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubOrderRepositoryPort {

    SubOrder save(SubOrder subOrder);

    Optional<SubOrder> findById(UUID id);

    /** Loads the sub-order together with its parent order (used when a delivery closes an order). */
    Optional<SubOrder> findByIdWithOrder(UUID id);

    /** Dispatch queue of one seller, filtered by fulfilment status. */
    Page<SubOrder> findBySellerIdAndStatus(UUID sellerId, SubOrderStatus status, Pageable pageable);

    Page<SubOrder> findBySellerId(UUID sellerId, Pageable pageable);

    /**
     * Used by review verification to prove the buyer actually received the product. Takes a page of
     * matching ids and loads those aggregates with their lines.
     */
    List<SubOrder> findWithItemsByIds(List<UUID> ids);

    List<UUID> findIdsByBuyerIdAndStatus(UUID buyerId, SubOrderStatus status);
}

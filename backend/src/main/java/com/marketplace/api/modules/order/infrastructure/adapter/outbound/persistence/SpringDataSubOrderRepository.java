package com.marketplace.api.modules.order.infrastructure.adapter.outbound.persistence;

import com.marketplace.api.modules.order.domain.model.SubOrder;
import com.marketplace.api.modules.order.domain.model.SubOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataSubOrderRepository extends JpaRepository<SubOrder, UUID> {

    @EntityGraph(attributePaths = {"order"})
    Page<SubOrder> findBySellerId(UUID sellerId, Pageable pageable);

    @EntityGraph(attributePaths = {"order"})
    Page<SubOrder> findBySellerIdAndStatus(UUID sellerId, SubOrderStatus status, Pageable pageable);

    /**
     * Loads a sub-order together with its parent order so that the parent can be closed when the
     * last delivery is confirmed, without relying on lazy loading outside the transaction.
     */
    @Query("select s from SubOrder s join fetch s.order where s.id = :id")
    Optional<SubOrder> findByIdWithOrder(@Param("id") UUID id);

    /**
     * Two-step fetch used to review-verify a purchase: page through the matching ids, then load
     * those aggregates with their lines. This avoids the classic "fetch join + pagination"
     * in-memory pagination problem.
     */
    @Query("select s.id from SubOrder s where s.order.buyerId = :buyerId and s.status = :status")
    List<UUID> findIdsByBuyerIdAndStatus(@Param("buyerId") UUID buyerId, @Param("status") SubOrderStatus status);

    @EntityGraph(attributePaths = {"items"})
    List<SubOrder> findWithItemsByIdIn(List<UUID> ids);
}

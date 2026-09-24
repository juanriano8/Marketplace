package com.marketplace.api.modules.order.infrastructure.adapter.outbound.persistence;

import com.marketplace.api.modules.order.domain.model.SubOrder;
import com.marketplace.api.modules.order.domain.model.SubOrderStatus;
import com.marketplace.api.modules.order.domain.port.outbound.SubOrderRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SubOrderPersistenceAdapter implements SubOrderRepositoryPort {

    private final SpringDataSubOrderRepository springDataSubOrderRepository;

    @Override
    public SubOrder save(SubOrder subOrder) {
        return springDataSubOrderRepository.save(subOrder);
    }

    @Override
    public Optional<SubOrder> findById(UUID id) {
        return springDataSubOrderRepository.findById(id);
    }

    @Override
    public Optional<SubOrder> findByIdWithOrder(UUID id) {
        return springDataSubOrderRepository.findByIdWithOrder(id);
    }

    @Override
    public Page<SubOrder> findBySellerIdAndStatus(UUID sellerId, SubOrderStatus status, Pageable pageable) {
        return springDataSubOrderRepository.findBySellerIdAndStatus(sellerId, status, pageable);
    }

    @Override
    public Page<SubOrder> findBySellerId(UUID sellerId, Pageable pageable) {
        return springDataSubOrderRepository.findBySellerId(sellerId, pageable);
    }

    @Override
    public List<SubOrder> findWithItemsByIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return springDataSubOrderRepository.findWithItemsByIdIn(ids);
    }

    @Override
    public List<UUID> findIdsByBuyerIdAndStatus(UUID buyerId, SubOrderStatus status) {
        return springDataSubOrderRepository.findIdsByBuyerIdAndStatus(buyerId, status);
    }
}

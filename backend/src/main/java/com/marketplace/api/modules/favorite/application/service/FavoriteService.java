package com.marketplace.api.modules.favorite.application.service;

import com.marketplace.api.modules.favorite.domain.model.Favorite;
import com.marketplace.api.modules.favorite.domain.port.inbound.FavoriteUseCase;
import com.marketplace.api.modules.favorite.domain.port.outbound.FavoriteRepositoryPort;
import com.marketplace.api.modules.product.application.dto.ProductResponse;
import com.marketplace.api.modules.product.domain.port.inbound.ProductUseCase;
import com.marketplace.api.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteService implements FavoriteUseCase {

    private final FavoriteRepositoryPort favoriteRepositoryPort;
    private final ProductUseCase productUseCase;

    @Override
    @Transactional
    public void add(UUID buyerId, UUID productId) {
        // Valida que el producto exista y esté activo antes de guardarlo.
        productUseCase.getProductById(productId);

        if (!favoriteRepositoryPort.existsByUserIdAndProductId(buyerId, productId)) {
            favoriteRepositoryPort.save(new Favorite(buyerId, productId));
            log.debug("Buyer {} added product {} to favorites", buyerId, productId);
        }
    }

    @Override
    @Transactional
    public void remove(UUID buyerId, UUID productId) {
        favoriteRepositoryPort.findByUserIdAndProductId(buyerId, productId)
            .ifPresent(favoriteRepositoryPort::delete);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> list(UUID buyerId) {
        return favoriteRepositoryPort.findProductIdsByUserId(buyerId).stream()
            .map(this::toProductOrNull)
            .filter(java.util.Objects::nonNull)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> ids(UUID buyerId) {
        return favoriteRepositoryPort.findProductIdsByUserId(buyerId);
    }

    private ProductResponse toProductOrNull(UUID productId) {
        try {
            return productUseCase.getProductById(productId);
        } catch (ResourceNotFoundException exception) {
            // El producto pudo desactivarse después de guardarse como favorito.
            return null;
        }
    }
}

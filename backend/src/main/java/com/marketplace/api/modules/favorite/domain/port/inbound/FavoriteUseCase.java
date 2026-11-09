package com.marketplace.api.modules.favorite.domain.port.inbound;

import com.marketplace.api.modules.product.application.dto.ProductResponse;

import java.util.List;
import java.util.UUID;

public interface FavoriteUseCase {

    void add(UUID buyerId, UUID productId);

    void remove(UUID buyerId, UUID productId);

    List<ProductResponse> list(UUID buyerId);

    List<UUID> ids(UUID buyerId);
}

package com.marketplace.api.modules.favorite.application.service;

import com.marketplace.api.modules.favorite.domain.model.Favorite;
import com.marketplace.api.modules.favorite.domain.port.outbound.FavoriteRepositoryPort;
import com.marketplace.api.modules.product.application.dto.ProductResponse;
import com.marketplace.api.modules.product.domain.port.inbound.ProductUseCase;
import com.marketplace.api.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FavoriteServiceTest {

    private FavoriteRepositoryPort favorites;
    private ProductUseCase products;
    private FavoriteService service;

    @BeforeEach
    void setUp() {
        favorites = mock(FavoriteRepositoryPort.class);
        products = mock(ProductUseCase.class);
        service = new FavoriteService(favorites, products);
    }

    @Test
    void agregarEsIdempotente() {
        UUID buyerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        when(products.getProductById(productId)).thenReturn(null);
        when(favorites.existsByUserIdAndProductId(buyerId, productId)).thenReturn(false);

        service.add(buyerId, productId);
        verify(favorites).save(any(Favorite.class));

        when(favorites.existsByUserIdAndProductId(buyerId, productId)).thenReturn(true);
        service.add(buyerId, productId);
        verify(favorites).save(any(Favorite.class));
    }

    @Test
    void quitarBorraSiExiste() {
        UUID buyerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Favorite favorite = new Favorite(buyerId, productId);

        when(favorites.findByUserIdAndProductId(buyerId, productId))
            .thenReturn(Optional.of(favorite));

        service.remove(buyerId, productId);

        verify(favorites).delete(favorite);
    }

    @Test
    void listarOmiteProductosQueYaNoEstanActivos() {
        UUID buyerId = UUID.randomUUID();
        UUID activo = UUID.randomUUID();
        UUID inactivo = UUID.randomUUID();
        ProductResponse response = new ProductResponse(
            activo, "Activo", null, "activo", null, "USD", 1,
            null, null, null, null, null, null
        );

        when(favorites.findProductIdsByUserId(buyerId)).thenReturn(List.of(activo, inactivo));
        when(products.getProductById(activo)).thenReturn(response);
        when(products.getProductById(inactivo))
            .thenThrow(new ResourceNotFoundException("not found"));

        assertThat(service.list(buyerId)).containsExactly(response);
    }

    @Test
    void idsDelegaEnElRepositorio() {
        UUID buyerId = UUID.randomUUID();
        List<UUID> ids = List.of(UUID.randomUUID(), UUID.randomUUID());

        when(favorites.findProductIdsByUserId(buyerId)).thenReturn(ids);

        assertThat(service.ids(buyerId)).isEqualTo(ids);
        verify(favorites, never()).save(any());
    }
}

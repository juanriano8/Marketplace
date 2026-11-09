package com.marketplace.api.modules.seller.application.service;

import com.marketplace.api.modules.seller.domain.port.outbound.SellerProfileRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

class SellerRatingWriterTest {

    private SellerProfileRepositoryPort repository;
    private SellerRatingWriter writer;

    @BeforeEach
    void setUp() {
        repository = mock(SellerProfileRepositoryPort.class);
        writer = new SellerRatingWriter(repository);
    }

    @Test
    void acumulaDeltasYLosVuelcaEnUnSoloUpsert() {
        UUID sellerId = UUID.randomUUID();

        writer.record(sellerId, 5, 1);
        writer.record(sellerId, 4, 1);
        writer.record(sellerId, 5, -1);

        assertThat(writer.pendingSellers()).isEqualTo(1);
        writer.flush();

        verify(repository).applyRatingDelta(sellerId, 4, 1);
        assertThat(writer.pendingSellers()).isZero();
    }

    @Test
    void siElVolcadoFallaReencolaElDelta() {
        UUID sellerId = UUID.randomUUID();
        doThrow(new RuntimeException("db caida"))
            .when(repository).applyRatingDelta(sellerId, 5, 1);

        writer.record(sellerId, 5, 1);
        writer.flush();

        assertThat(writer.pendingSellers()).isEqualTo(1);

        reset(repository);
        writer.flush();

        verify(repository).applyRatingDelta(sellerId, 5, 1);
        assertThat(writer.pendingSellers()).isZero();
    }

    @Test
    void ignoraEventosSinVendedorOCountCero() {
        writer.record(null, 5, 1);
        writer.record(UUID.randomUUID(), 5, 0);

        assertThat(writer.pendingSellers()).isZero();
    }
}

package com.marketplace.api.modules.seller.application.service;

import com.marketplace.api.modules.seller.domain.port.outbound.SellerProfileRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Escritor write-behind del rating de vendedor.
 *
 * <p>Las reseñas no actualizan la fila del vendedor en la misma transacción: dejan un delta en
 * memoria y un job lo vuelca cada pocos segundos. Así una ráfaga de reseñas no bloquea la
 * creación de reseñas ni compite por la misma fila.</p>
 *
 * <p>Si el volcado falla, el delta se reencola para el siguiente intento. Si el proceso cae antes
 * del volcado, se pierde ese delta; es el compromiso habitual de un write-behind en memoria. Para
 * durabilidad total habría que usar una cola persistente (Redis Streams, Kafka, etc.).</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SellerRatingWriter {

    private final SellerProfileRepositoryPort sellerProfileRepositoryPort;
    private final ConcurrentHashMap<UUID, Delta> pending = new ConcurrentHashMap<>();

    /** Encola un cambio de reseña: +rating al crear, -rating al borrar. */
    public void record(UUID sellerId, int rating, int countDelta) {
        if (sellerId == null || countDelta == 0) {
            return;
        }

        pending.compute(sellerId, (key, current) -> {
            Delta base = current != null ? current : new Delta(0, 0);
            return new Delta(base.sum() + (long) rating * countDelta, base.count() + countDelta);
        });
    }

    /** Vuelca los deltas acumulados. Es idempotente a nivel de lote. */
    @Scheduled(fixedDelayString = "${app.seller-rating.flush-ms:5000}")
    public void flush() {
        for (UUID sellerId : pending.keySet()) {
            Delta delta = pending.remove(sellerId);
            if (delta == null) {
                continue;
            }

            try {
                sellerProfileRepositoryPort.applyRatingDelta(sellerId, delta.sum(), delta.count());
                log.debug("Rating write-behind aplicado para {}: sum={}, count={}",
                    sellerId, delta.sum(), delta.count());
            } catch (Exception exception) {
                pending.merge(sellerId, delta, Delta::plus);
                log.error("No se pudo volcar el rating de {}; se reintentará: {}",
                    sellerId, exception.getMessage());
            }
        }
    }

    /** Visible para tests y diagnóstico. */
    public int pendingSellers() {
        return pending.size();
    }

    record Delta(long sum, long count) {
        Delta plus(Delta other) {
            return new Delta(sum + other.sum, count + other.count);
        }
    }
}

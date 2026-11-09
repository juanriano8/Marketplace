package com.marketplace.api.modules.seller.infrastructure.event;

import com.marketplace.api.modules.seller.application.service.SellerRatingWriter;
import com.marketplace.api.shared.events.SellerRatingEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Encola el cambio de rating después de que la reseña quedó confirmada.
 * No escribe en la base de datos: eso lo hace el job de {@link SellerRatingWriter}.
 */
@Component
@RequiredArgsConstructor
public class SellerRatingEventListener {

    private final SellerRatingWriter sellerRatingWriter;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSellerRatingChanged(SellerRatingEvent event) {
        sellerRatingWriter.record(event.sellerId(), event.rating(), event.countDelta());
    }
}

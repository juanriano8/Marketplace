package com.marketplace.api.shared.events;

import java.util.UUID;

/**
 * Cambio de rating de un vendedor. Se publica al crear (+1) o borrar (-1) una reseña.
 *
 * <p>El delta no se escribe en la misma transacción: lo acumula {@code SellerRatingWriter} y lo
 * vuelca un job periódico (write-behind).</p>
 */
public record SellerRatingEvent(UUID sellerId, int rating, int countDelta) {
}

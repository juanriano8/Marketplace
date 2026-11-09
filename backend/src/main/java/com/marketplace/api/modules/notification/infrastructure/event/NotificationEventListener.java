package com.marketplace.api.modules.notification.infrastructure.event;

import com.marketplace.api.modules.notification.domain.port.inbound.NotificationUseCase;
import com.marketplace.api.shared.events.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Convierte eventos de dominio en notificaciones internas.
 *
 * <p>Se ejecuta después del commit y en un hilo aparte: si la notificación
 * falla, la operación de negocio ya quedó confirmada y el error solo se registra.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationUseCase notificationUseCase;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onNotification(NotificationEvent event) {
        try {
            notificationUseCase.notify(
                event.userId(),
                event.type(),
                event.title(),
                event.message(),
                event.link()
            );
        } catch (Exception exception) {
            log.error("No se pudo crear la notificación {} para {}: {}",
                event.type(), event.userId(), exception.getMessage());
        }
    }
}

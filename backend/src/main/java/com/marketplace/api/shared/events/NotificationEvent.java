package com.marketplace.api.shared.events;

import com.marketplace.api.modules.notification.domain.model.NotificationType;

import java.util.UUID;

/**
 * Evento genérico para crear una notificación interna.
 *
 * <p>Los módulos de negocio publican este evento y el módulo de notificaciones
 * lo consume después del commit, de modo que un fallo al notificar no revierte
 * la operación principal.</p>
 */
public record NotificationEvent(
    UUID userId,
    NotificationType type,
    String title,
    String message,
    String link
) {
}

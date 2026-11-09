package com.marketplace.api.modules.notification.application.dto;

import com.marketplace.api.modules.notification.domain.model.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Notificación interna de un usuario")
public record NotificationResponse(
    UUID id,
    NotificationType type,
    String title,
    String message,
    String link,
    boolean read,
    Instant createdAt
) {}

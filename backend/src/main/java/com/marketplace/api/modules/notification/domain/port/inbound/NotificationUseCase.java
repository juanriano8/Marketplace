package com.marketplace.api.modules.notification.domain.port.inbound;

import com.marketplace.api.modules.notification.application.dto.NotificationResponse;
import com.marketplace.api.modules.notification.domain.model.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationUseCase {

    Page<NotificationResponse> list(UUID userId, Pageable pageable);

    long unreadCount(UUID userId);

    void markRead(UUID userId, UUID notificationId);

    int markAllRead(UUID userId);

    /** Crea una notificación para un usuario; lo usan los listeners de eventos. */
    void notify(UUID userId, NotificationType type, String title, String message, String link);
}

package com.marketplace.api.modules.notification.application.service;

import com.marketplace.api.modules.notification.application.dto.NotificationResponse;
import com.marketplace.api.modules.notification.application.mapper.NotificationMapper;
import com.marketplace.api.modules.notification.domain.model.Notification;
import com.marketplace.api.modules.notification.domain.model.NotificationType;
import com.marketplace.api.modules.notification.domain.port.inbound.NotificationUseCase;
import com.marketplace.api.modules.notification.domain.port.outbound.NotificationRepositoryPort;
import com.marketplace.api.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService implements NotificationUseCase {

    private final NotificationRepositoryPort notificationRepositoryPort;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> list(UUID userId, Pageable pageable) {
        return notificationRepositoryPort.findByUserId(userId, pageable)
            .map(notificationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public long unreadCount(UUID userId) {
        return notificationRepositoryPort.countUnread(userId);
    }

    @Override
    @Transactional
    public void markRead(UUID userId, UUID notificationId) {
        Notification notification = notificationRepositoryPort.findByIdAndUserId(notificationId, userId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Notification not found with id: " + notificationId
            ));

        notification.markRead();
        notificationRepositoryPort.save(notification);
    }

    @Override
    @Transactional
    public int markAllRead(UUID userId) {
        return notificationRepositoryPort.markAllRead(userId);
    }

    @Override
    @Transactional
    public void notify(UUID userId, NotificationType type, String title, String message, String link) {
        if (userId == null) {
            log.warn("Se intentó crear una notificación sin destinatario: {}", type);
            return;
        }

        Notification notification = new Notification(userId, type, title, message, link);
        notificationRepositoryPort.save(notification);
        log.debug("Notificación {} creada para {}", type, userId);
    }
}

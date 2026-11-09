package com.marketplace.api.modules.notification.domain.port.outbound;

import com.marketplace.api.modules.notification.domain.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface NotificationRepositoryPort {

    Notification save(Notification notification);

    Page<Notification> findByUserId(UUID userId, Pageable pageable);

    long countUnread(UUID userId);

    Optional<Notification> findByIdAndUserId(UUID notificationId, UUID userId);

    int markAllRead(UUID userId);
}

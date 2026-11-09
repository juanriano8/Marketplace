package com.marketplace.api.modules.notification.infrastructure.adapter.outbound.persistence;

import com.marketplace.api.modules.notification.domain.model.Notification;
import com.marketplace.api.modules.notification.domain.port.outbound.NotificationRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NotificationPersistenceAdapter implements NotificationRepositoryPort {

    private final SpringDataNotificationRepository repository;

    @Override
    public Notification save(Notification notification) {
        return repository.save(notification);
    }

    @Override
    public Page<Notification> findByUserId(UUID userId, Pageable pageable) {
        return repository.findByUserId(userId, pageable);
    }

    @Override
    public long countUnread(UUID userId) {
        return repository.countByUserIdAndReadFalse(userId);
    }

    @Override
    public Optional<Notification> findByIdAndUserId(UUID notificationId, UUID userId) {
        return repository.findByIdAndUserId(notificationId, userId);
    }

    @Override
    public int markAllRead(UUID userId) {
        return repository.markAllRead(userId, Instant.now());
    }
}

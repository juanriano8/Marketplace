package com.marketplace.api.modules.notification.domain.model;

import com.marketplace.api.shared.domain.BaseEntity;
import com.marketplace.api.shared.exception.DomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/** Notificación interna de un usuario. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "notifications",
    indexes = {
        @Index(name = "idx_notifications_user_created", columnList = "user_id, created_at"),
        @Index(name = "idx_notifications_user_unread", columnList = "user_id, is_read")
    }
)
public class Notification extends BaseEntity {

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 40)
    private NotificationType type;

    @Column(name = "title", nullable = false, length = 160)
    private String title;

    @Column(name = "message", nullable = false, length = 500)
    private String message;

    @Column(name = "link", length = 255)
    private String link;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @Column(name = "read_at")
    private Instant readAt;

    public Notification(UUID userId, NotificationType type, String title, String message, String link) {
        if (userId == null) {
            throw new DomainException("A notification requires a recipient");
        }
        if (type == null) {
            throw new DomainException("A notification requires a type");
        }
        if (title == null || title.isBlank()) {
            throw new DomainException("A notification requires a title");
        }
        if (message == null || message.isBlank()) {
            throw new DomainException("A notification requires a message");
        }

        this.userId = userId;
        this.type = type;
        this.title = title.strip();
        this.message = message.strip();
        this.link = link;
        this.read = false;
    }

    /** Marca la notificación como leída; es idempotente. */
    public void markRead() {
        if (!this.read) {
            this.read = true;
            this.readAt = Instant.now();
        }
    }
}

package com.marketplace.api.modules.notification.application.service;

import com.marketplace.api.modules.notification.application.dto.NotificationResponse;
import com.marketplace.api.modules.notification.application.mapper.NotificationMapper;
import com.marketplace.api.modules.notification.domain.model.Notification;
import com.marketplace.api.modules.notification.domain.model.NotificationType;
import com.marketplace.api.modules.notification.domain.port.outbound.NotificationRepositoryPort;
import com.marketplace.api.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepositoryPort notificationRepositoryPort;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void notificaGuardaUnaNotificacionNoLeida() {
        UUID sellerId = UUID.randomUUID();

        notificationService.notify(
            sellerId,
            NotificationType.PRODUCT_APPROVED,
            "Producto aprobado",
            "Tu producto ya está visible.",
            "/vendedor"
        );

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepositoryPort).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(sellerId);
        assertThat(saved.getType()).isEqualTo(NotificationType.PRODUCT_APPROVED);
        assertThat(saved.isRead()).isFalse();
    }

    @Test
    void noGuardaSiNoHayDestinatario() {
        notificationService.notify(null, NotificationType.PRODUCT_APPROVED, "T", "M", null);

        verify(notificationRepositoryPort, never()).save(any());
    }

    @Test
    void marcarComoLeidaSoloAceptaNotificacionesDelUsuario() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        Notification notification = new Notification(
            userId, NotificationType.ORDER_SHIPPED, "Pedido despachado", "Tu pedido salió.", "/mis-compras"
        );

        when(notificationRepositoryPort.findByIdAndUserId(notificationId, userId))
            .thenReturn(Optional.of(notification));

        notificationService.markRead(userId, notificationId);

        assertThat(notification.isRead()).isTrue();
        assertThat(notification.getReadAt()).isNotNull();
        verify(notificationRepositoryPort).save(notification);
    }

    @Test
    void notificacionDeOtroUsuarioDevuelve404() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        when(notificationRepositoryPort.findByIdAndUserId(notificationId, userId))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markRead(userId, notificationId))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listaUsaElMapper() {
        UUID userId = UUID.randomUUID();
        Notification notification = new Notification(
            userId, NotificationType.REVIEW_RECEIVED, "Nueva reseña", "Recibiste 5★.", "/vendedor"
        );
        NotificationResponse response = new NotificationResponse(
            UUID.randomUUID(), notification.getType(), notification.getTitle(),
            notification.getMessage(), notification.getLink(), false, Instant.now()
        );

        when(notificationRepositoryPort.findByUserId(any(), any()))
            .thenReturn(new org.springframework.data.domain.PageImpl<>(java.util.List.of(notification)));
        when(notificationMapper.toResponse(notification)).thenReturn(response);

        var page = notificationService.list(userId, org.springframework.data.domain.PageRequest.of(0, 20));

        assertThat(page.getContent()).containsExactly(response);
    }
}

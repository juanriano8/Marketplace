package com.marketplace.api.modules.notification.infrastructure.adapter.inbound.rest;

import com.marketplace.api.modules.notification.application.dto.NotificationResponse;
import com.marketplace.api.modules.notification.domain.port.inbound.NotificationUseCase;
import com.marketplace.api.shared.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notificaciones internas del usuario autenticado")
@SecurityRequirement(name = "BearerAuth")
public class NotificationController {

    private final NotificationUseCase notificationUseCase;

    @Operation(summary = "Listar mis notificaciones")
    @ApiResponse(responseCode = "200", description = "Página de notificaciones")
    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> list(
        @AuthenticationPrincipal UserPrincipal principal,
        @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable
    ) {
        return ResponseEntity.ok(notificationUseCase.list(principal.id(), pageable));
    }

    @Operation(summary = "Contar notificaciones sin leer")
    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountResponse> unreadCount(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(new UnreadCountResponse(notificationUseCase.unreadCount(principal.id())));
    }

    @Operation(summary = "Marcar una notificación como leída")
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markRead(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable UUID id
    ) {
        notificationUseCase.markRead(principal.id(), id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Marcar todas mis notificaciones como leídas")
    @PatchMapping("/read-all")
    public ResponseEntity<MarkAllReadResponse> markAllRead(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(new MarkAllReadResponse(notificationUseCase.markAllRead(principal.id())));
    }

    public record UnreadCountResponse(long count) {
    }

    public record MarkAllReadResponse(int updated) {
    }
}

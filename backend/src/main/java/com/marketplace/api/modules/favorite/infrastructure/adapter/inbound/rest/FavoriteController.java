package com.marketplace.api.modules.favorite.infrastructure.adapter.inbound.rest;

import com.marketplace.api.modules.favorite.domain.port.inbound.FavoriteUseCase;
import com.marketplace.api.modules.product.application.dto.ProductResponse;
import com.marketplace.api.shared.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
@PreAuthorize("hasRole('BUYER')")
@Tag(name = "Favorites", description = "Productos guardados por el comprador")
@SecurityRequirement(name = "BearerAuth")
public class FavoriteController {

    private final FavoriteUseCase favoriteUseCase;

    @Operation(summary = "Listar mis favoritos")
    @GetMapping
    public ResponseEntity<List<ProductResponse>> list(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(favoriteUseCase.list(principal.id()));
    }

    @Operation(summary = "IDs de mis favoritos (para pintar corazones)")
    @GetMapping("/ids")
    public ResponseEntity<List<UUID>> ids(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(favoriteUseCase.ids(principal.id()));
    }

    @Operation(summary = "Guardar un producto en favoritos")
    @PostMapping("/{productId}")
    public ResponseEntity<Void> add(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable UUID productId
    ) {
        favoriteUseCase.add(principal.id(), productId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Quitar un producto de favoritos")
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> remove(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable UUID productId
    ) {
        favoriteUseCase.remove(principal.id(), productId);
        return ResponseEntity.noContent().build();
    }
}

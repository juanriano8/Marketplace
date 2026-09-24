package com.marketplace.api.modules.inventory.infrastructure.adapter.inbound.rest;

import com.marketplace.api.modules.inventory.application.dto.StockMovementResponse;
import com.marketplace.api.modules.inventory.domain.port.inbound.StockQueryPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Inventory - Admin", description = "Global inventory auditing for administrators")
@SecurityRequirement(name = "BearerAuth")
public class AdminInventoryController {

    private final StockQueryPort stockQueryPort;

    @Operation(
        summary = "Global inventory audit",
        description = "Append-only audit trail of every stock movement across the marketplace, optionally filtered by product."
    )
    @ApiResponse(responseCode = "200", description = "Audit page returned")
    @GetMapping("/audit")
    public ResponseEntity<Page<StockMovementResponse>> audit(
        @RequestParam(required = false) UUID productId,
        @ParameterObject @PageableDefault(size = 50, sort = "createdAt") Pageable pageable
    ) {
        Page<StockMovementResponse> page = productId == null
            ? stockQueryPort.auditMovements(pageable)
            : stockQueryPort.auditMovementsByProduct(productId, pageable);

        return ResponseEntity.ok(page);
    }
}

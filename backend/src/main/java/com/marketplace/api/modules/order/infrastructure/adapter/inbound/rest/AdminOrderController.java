package com.marketplace.api.modules.order.infrastructure.adapter.inbound.rest;

import com.marketplace.api.modules.order.application.dto.OrderResponse;
import com.marketplace.api.modules.order.domain.port.inbound.OrderQueryUseCase;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Orders - Admin", description = "Global transaction monitoring")
@SecurityRequirement(name = "BearerAuth")
public class AdminOrderController {

    private final OrderQueryUseCase orderQueryUseCase;

    @Operation(
        summary = "List all transactions",
        description = "Paginated global view of every order in the marketplace, including per-seller sub-orders and payment references."
    )
    @ApiResponse(responseCode = "200", description = "Order page returned")
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> listAllOrders(
        @ParameterObject @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(orderQueryUseCase.listAllOrders(pageable));
    }
}

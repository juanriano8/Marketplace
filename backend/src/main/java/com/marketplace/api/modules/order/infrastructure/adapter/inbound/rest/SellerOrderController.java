package com.marketplace.api.modules.order.infrastructure.adapter.inbound.rest;

import com.marketplace.api.modules.order.application.dto.OrderResponse;
import com.marketplace.api.modules.order.application.dto.ShipSubOrderRequest;
import com.marketplace.api.modules.order.domain.model.SubOrderStatus;
import com.marketplace.api.modules.order.domain.port.inbound.OrderQueryUseCase;
import com.marketplace.api.shared.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/seller/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SELLER')")
@Tag(name = "Orders - Seller", description = "Seller dispatch queue and shipping updates")
@SecurityRequirement(name = "BearerAuth")
public class SellerOrderController {

    private final OrderQueryUseCase orderQueryUseCase;

    @Operation(
        summary = "List own dispatch queue",
        description = "Paginated sub-orders belonging to the authenticated seller. Defaults to PROCESSING (pending dispatch); pass status to inspect other fulfilment states."
    )
    @ApiResponse(responseCode = "200", description = "Sub-order page returned")
    @GetMapping
    public ResponseEntity<Page<OrderResponse.SubOrderResponse>> listSubOrders(
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestParam(required = false) SubOrderStatus status,
        @ParameterObject @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(orderQueryUseCase.listSellerSubOrders(principal.id(), status, pageable));
    }

    @Operation(
        summary = "Register a dispatch",
        description = "Marks the seller's sub-order as SHIPPED and stores the tracking number. Only sub-orders in PROCESSING can be shipped, and only by their owning seller."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sub-order shipped"),
        @ApiResponse(responseCode = "400", description = "Invalid state transition or the sub-order belongs to another seller"),
        @ApiResponse(responseCode = "404", description = "Sub-order not found")
    })
    @PatchMapping("/{subOrderId}/ship")
    public ResponseEntity<OrderResponse.SubOrderResponse> shipSubOrder(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable UUID subOrderId,
        @Valid @RequestBody ShipSubOrderRequest request
    ) {
        return ResponseEntity.ok(orderQueryUseCase.shipSubOrder(
            principal.id(),
            subOrderId,
            request.trackingNumber(),
            request.carrier()
        ));
    }
}

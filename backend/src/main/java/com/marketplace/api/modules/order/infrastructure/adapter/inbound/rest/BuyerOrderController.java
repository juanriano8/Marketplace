package com.marketplace.api.modules.order.infrastructure.adapter.inbound.rest;

import com.marketplace.api.modules.order.application.dto.CheckoutRequest;
import com.marketplace.api.modules.order.application.dto.CheckoutResponse;
import com.marketplace.api.modules.order.application.dto.OrderResponse;
import com.marketplace.api.modules.order.domain.port.inbound.CheckoutUseCase;
import com.marketplace.api.modules.order.domain.port.inbound.OrderQueryUseCase;
import com.marketplace.api.shared.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@PreAuthorize("hasRole('BUYER')")
@Tag(name = "Orders - Buyer", description = "Buyer checkout and purchase history")
@SecurityRequirement(name = "BearerAuth")
public class BuyerOrderController {

    private final CheckoutUseCase checkoutUseCase;
    private final OrderQueryUseCase orderQueryUseCase;

    @Operation(
        summary = "Checkout the active cart",
        description = "Creates the order (split per seller), reserves stock, charges through the payment gateway and returns the resulting order."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Order created; check paymentCaptured for the outcome"),
        @ApiResponse(responseCode = "400", description = "Empty cart, unavailable product, mixed currencies or insufficient stock"),
        @ApiResponse(responseCode = "404", description = "Cart or buyer not found"),
        @ApiResponse(responseCode = "409", description = "Concurrent modification while reserving stock")
    })
    @PostMapping("/orders/checkout")
    public ResponseEntity<CheckoutResponse> checkout(
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestBody(required = false) CheckoutRequest request
    ) {
        String notes = request != null ? request.notes() : null;
        CheckoutResponse response = checkoutUseCase.checkout(principal.id(), notes);

        // 201 Created: the request creates the order resource. The status code is
        // "201" per the API contract.
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "List own orders", description = "Paginated purchase history of the authenticated buyer.")
    @ApiResponse(responseCode = "200", description = "Order page returned")
    @GetMapping("/buyer/orders")
    public ResponseEntity<Page<OrderResponse>> listOwnOrders(
        @AuthenticationPrincipal UserPrincipal principal,
        @ParameterObject @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(orderQueryUseCase.listBuyerOrders(principal.id(), pageable));
    }

    @Operation(summary = "Get one own order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order returned"),
        @ApiResponse(responseCode = "404", description = "Order not found for this buyer")
    })
    @GetMapping("/buyer/orders/{orderId}")
    public ResponseEntity<OrderResponse> getOwnOrder(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable UUID orderId
    ) {
        return ResponseEntity.ok(orderQueryUseCase.getBuyerOrder(principal.id(), orderId));
    }
}

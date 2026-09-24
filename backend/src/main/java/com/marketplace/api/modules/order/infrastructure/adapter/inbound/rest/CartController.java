package com.marketplace.api.modules.order.infrastructure.adapter.inbound.rest;

import com.marketplace.api.modules.order.application.dto.AddCartItemRequest;
import com.marketplace.api.modules.order.application.dto.CartResponse;
import com.marketplace.api.modules.order.application.dto.UpdateCartItemRequest;
import com.marketplace.api.modules.order.domain.port.inbound.CartUseCase;
import com.marketplace.api.shared.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@PreAuthorize("hasRole('BUYER')")
@Tag(name = "Cart", description = "Buyer shopping cart")
@SecurityRequirement(name = "BearerAuth")
public class CartController {

    private final CartUseCase cartUseCase;

    @Operation(summary = "Get the active cart", description = "Returns the authenticated buyer's active cart, creating an empty one on first access.")
    @ApiResponse(responseCode = "200", description = "Cart returned")
    @GetMapping
    public ResponseEntity<CartResponse> getActiveCart(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(cartUseCase.getActiveCart(principal.id()));
    }

    @Operation(summary = "Add an item to the cart", description = "Validates that the product is ACTIVE and has sellable stock before adding it.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cart updated"),
        @ApiResponse(responseCode = "400", description = "Product not purchasable or insufficient stock"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody AddCartItemRequest request
    ) {
        return ResponseEntity.ok(cartUseCase.addItem(principal.id(), request.productId(), request.quantity()));
    }

    @Operation(summary = "Update a cart line", description = "Sets the absolute quantity of a line; quantity 0 removes it.")
    @ApiResponse(responseCode = "200", description = "Cart updated")
    @PutMapping("/items/{productId}")
    public ResponseEntity<CartResponse> updateItem(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable UUID productId,
        @Valid @RequestBody UpdateCartItemRequest request
    ) {
        return ResponseEntity.ok(cartUseCase.updateItemQuantity(principal.id(), productId, request.quantity()));
    }

    @Operation(summary = "Remove a cart line")
    @ApiResponse(responseCode = "200", description = "Cart updated")
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponse> removeItem(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable UUID productId
    ) {
        return ResponseEntity.ok(cartUseCase.removeItem(principal.id(), productId));
    }

    @Operation(summary = "Empty the cart")
    @ApiResponse(responseCode = "200", description = "Cart emptied")
    @DeleteMapping
    public ResponseEntity<CartResponse> clearCart(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(cartUseCase.clearCart(principal.id()));
    }
}

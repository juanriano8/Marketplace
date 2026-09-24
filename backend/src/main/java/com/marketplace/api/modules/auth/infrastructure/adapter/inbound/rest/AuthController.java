package com.marketplace.api.modules.auth.infrastructure.adapter.inbound.rest;

import com.marketplace.api.modules.auth.application.dto.AuthResponse;
import com.marketplace.api.modules.auth.application.dto.LoginRequest;
import com.marketplace.api.modules.auth.application.dto.RegisterBuyerRequest;
import com.marketplace.api.modules.auth.application.dto.RegisterSellerRequest;
import com.marketplace.api.modules.auth.domain.port.inbound.AuthUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration and JWT authentication")
public class AuthController {

    private final AuthUseCase authUseCase;

    @Operation(summary = "Register a new buyer", description = "Registers a new user with ROLE_BUYER role and returns an access token.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Buyer successfully registered"),
        @ApiResponse(responseCode = "400", description = "Invalid request payload or email already exists")
    })
    @PostMapping("/register/buyer")
    public ResponseEntity<AuthResponse> registerBuyer(@Valid @RequestBody RegisterBuyerRequest request) {
        AuthResponse response = authUseCase.registerBuyer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Register a new seller", description = "Registers a new seller request with ROLE_SELLER. Account requires admin verification.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Seller registration submitted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request payload or email already exists")
    })
    @PostMapping("/register/seller")
    public ResponseEntity<AuthResponse> registerSeller(@Valid @RequestBody RegisterSellerRequest request) {
        AuthResponse response = authUseCase.registerSeller(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "User login", description = "Authenticates user credentials and returns a JWT Bearer token.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Authentication successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "400", description = "Invalid request payload")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authUseCase.login(request);
        return ResponseEntity.ok(response);
    }
}

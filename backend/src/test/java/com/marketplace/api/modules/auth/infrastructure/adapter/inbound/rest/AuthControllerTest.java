package com.marketplace.api.modules.auth.infrastructure.adapter.inbound.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.api.config.SecurityConfig;
import com.marketplace.api.modules.auth.application.dto.AuthResponse;
import com.marketplace.api.modules.auth.application.dto.LoginRequest;
import com.marketplace.api.modules.auth.application.dto.RegisterBuyerRequest;
import com.marketplace.api.modules.auth.application.dto.RegisterSellerRequest;
import com.marketplace.api.modules.auth.domain.port.inbound.AuthUseCase;
import com.marketplace.api.shared.security.CustomUserDetailsService;
import com.marketplace.api.shared.security.JwtAuthenticationEntryPoint;
import com.marketplace.api.shared.security.JwtAuthenticationFilter;
import com.marketplace.api.shared.security.JwtTokenProvider;
import com.marketplace.api.shared.security.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthUseCase authUseCase;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Test
    @DisplayName("POST /api/v1/auth/register/buyer should return 201 Created")
    void shouldRegisterBuyer() throws Exception {
        RegisterBuyerRequest request = new RegisterBuyerRequest("buyer@test.com", "Password123!");
        AuthResponse response = AuthResponse.of("jwt.mock.token", 86400000L, UUID.randomUUID(), "buyer@test.com", UserRole.ROLE_BUYER, null);

        when(authUseCase.registerBuyer(any(RegisterBuyerRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register/buyer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.accessToken").value("jwt.mock.token"))
            .andExpect(jsonPath("$.email").value("buyer@test.com"))
            .andExpect(jsonPath("$.role").value("ROLE_BUYER"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register/seller should return 201 Created")
    void shouldRegisterSeller() throws Exception {
        RegisterSellerRequest request = new RegisterSellerRequest("seller@test.com", "Password123!");
        AuthResponse response = AuthResponse.of("jwt.mock.token", 86400000L, UUID.randomUUID(), "seller@test.com", UserRole.ROLE_SELLER, false);

        when(authUseCase.registerSeller(any(RegisterSellerRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register/seller")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.role").value("ROLE_SELLER"))
            .andExpect(jsonPath("$.sellerApproved").value(false));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login should return 200 OK")
    void shouldLogin() throws Exception {
        LoginRequest request = new LoginRequest("user@test.com", "Password123!");
        AuthResponse response = AuthResponse.of("jwt.mock.token", 86400000L, UUID.randomUUID(), "user@test.com", UserRole.ROLE_BUYER, null);

        when(authUseCase.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value("jwt.mock.token"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register/buyer with invalid email should return 400 Bad Request")
    void shouldFailValidationOnInvalidEmail() throws Exception {
        RegisterBuyerRequest request = new RegisterBuyerRequest("not-an-email", "Password123!");

        mockMvc.perform(post("/api/v1/auth/register/buyer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/auth/register/buyer with short password should return 400 Bad Request")
    void shouldFailValidationOnShortPassword() throws Exception {
        RegisterBuyerRequest request = new RegisterBuyerRequest("buyer@test.com", "short");

        mockMvc.perform(post("/api/v1/auth/register/buyer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
}

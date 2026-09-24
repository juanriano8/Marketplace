package com.marketplace.api.modules.auth.application.service;

import com.marketplace.api.modules.auth.application.dto.AuthResponse;
import com.marketplace.api.modules.auth.application.dto.LoginRequest;
import com.marketplace.api.modules.auth.application.dto.RegisterBuyerRequest;
import com.marketplace.api.modules.auth.application.dto.RegisterSellerRequest;
import com.marketplace.api.modules.user.domain.model.User;
import com.marketplace.api.modules.user.domain.port.outbound.UserRepositoryPort;
import com.marketplace.api.shared.exception.DomainException;
import com.marketplace.api.shared.security.JwtTokenProvider;
import com.marketplace.api.shared.security.UserPrincipal;
import com.marketplace.api.shared.security.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Should successfully register a new buyer")
    void shouldRegisterBuyerSuccessfully() {
        RegisterBuyerRequest request = new RegisterBuyerRequest("buyer@marketplace.com", "Password123!");
        when(userRepositoryPort.existsByEmail("buyer@marketplace.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("hashedPassword");

        User savedUser = new User("buyer@marketplace.com", "hashedPassword", UserRole.ROLE_BUYER);
        UUID userId = UUID.randomUUID();
        savedUser.setId(userId);

        when(userRepositoryPort.save(any(User.class))).thenReturn(savedUser);
        when(jwtTokenProvider.generateToken("buyer@marketplace.com", userId, UserRole.ROLE_BUYER)).thenReturn("jwt.token.buyer");
        when(jwtTokenProvider.getExpirationMs()).thenReturn(86400000L);

        AuthResponse response = authService.registerBuyer(request);

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("jwt.token.buyer");
        assertThat(response.email()).isEqualTo("buyer@marketplace.com");
        assertThat(response.role()).isEqualTo(UserRole.ROLE_BUYER);
        assertThat(response.sellerApproved()).isNull();
        verify(userRepositoryPort).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw DomainException when registering with duplicate email")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        RegisterBuyerRequest request = new RegisterBuyerRequest("existing@marketplace.com", "Password123!");
        when(userRepositoryPort.existsByEmail("existing@marketplace.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.registerBuyer(request))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("Email is already registered");
    }

    @Test
    @DisplayName("Should register seller with sellerApproved set to false")
    void shouldRegisterSellerWithPendingApproval() {
        RegisterSellerRequest request = new RegisterSellerRequest("seller@store.com", "Password123!");
        when(userRepositoryPort.existsByEmail("seller@store.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("hashedPassword");

        User savedUser = new User("seller@store.com", "hashedPassword", UserRole.ROLE_SELLER);
        UUID userId = UUID.randomUUID();
        savedUser.setId(userId);

        when(userRepositoryPort.save(any(User.class))).thenReturn(savedUser);
        when(jwtTokenProvider.generateToken("seller@store.com", userId, UserRole.ROLE_SELLER)).thenReturn("jwt.token.seller");
        when(jwtTokenProvider.getExpirationMs()).thenReturn(86400000L);

        AuthResponse response = authService.registerSeller(request);

        assertThat(response).isNotNull();
        assertThat(response.role()).isEqualTo(UserRole.ROLE_SELLER);
        assertThat(response.sellerApproved()).isFalse();
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void shouldLoginSuccessfully() {
        LoginRequest request = new LoginRequest("user@marketplace.com", "Password123!");
        UUID userId = UUID.randomUUID();
        UserPrincipal principal = new UserPrincipal(userId, "user@marketplace.com", "hashedPassword", UserRole.ROLE_BUYER, true);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        User user = new User("user@marketplace.com", "hashedPassword", UserRole.ROLE_BUYER);
        user.setId(userId);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userRepositoryPort.findByEmail("user@marketplace.com")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken(principal)).thenReturn("valid.jwt.token");
        when(jwtTokenProvider.getExpirationMs()).thenReturn(86400000L);

        AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("valid.jwt.token");
        assertThat(response.email()).isEqualTo("user@marketplace.com");
    }

    @Test
    @DisplayName("Should propagate BadCredentialsException when login fails")
    void shouldFailLoginWhenCredentialsInvalid() {
        LoginRequest request = new LoginRequest("user@marketplace.com", "WrongPassword");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(BadCredentialsException.class);
    }
}

package com.marketplace.api.modules.auth.application.service;

import com.marketplace.api.modules.auth.application.dto.AuthResponse;
import com.marketplace.api.modules.auth.application.dto.LoginRequest;
import com.marketplace.api.modules.auth.application.dto.RegisterBuyerRequest;
import com.marketplace.api.modules.auth.application.dto.RegisterSellerRequest;
import com.marketplace.api.modules.auth.domain.port.inbound.AuthUseCase;
import com.marketplace.api.modules.user.domain.model.User;
import com.marketplace.api.modules.user.domain.port.outbound.UserRepositoryPort;
import com.marketplace.api.shared.exception.DomainException;
import com.marketplace.api.shared.exception.ResourceNotFoundException;
import com.marketplace.api.shared.security.JwtTokenProvider;
import com.marketplace.api.shared.security.UserPrincipal;
import com.marketplace.api.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements AuthUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponse registerBuyer(RegisterBuyerRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepositoryPort.existsByEmail(email)) {
            throw new DomainException("Email is already registered: " + email);
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = new User(email, encodedPassword, UserRole.ROLE_BUYER);
        User savedUser = userRepositoryPort.save(user);

        log.info("Registered new buyer with id: {}", savedUser.getId());

        String token = jwtTokenProvider.generateToken(
            savedUser.getEmail(),
            savedUser.getId(),
            savedUser.getRole()
        );

        return AuthResponse.of(
            token,
            jwtTokenProvider.getExpirationMs(),
            savedUser.getId(),
            savedUser.getEmail(),
            savedUser.getRole(),
            savedUser.getSellerApproved()
        );
    }

    @Override
    @Transactional
    public AuthResponse registerSeller(RegisterSellerRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepositoryPort.existsByEmail(email)) {
            throw new DomainException("Email is already registered: " + email);
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = new User(email, encodedPassword, UserRole.ROLE_SELLER);
        User savedUser = userRepositoryPort.save(user);

        log.info("Registered new seller with id: {} (pending verification)", savedUser.getId());

        String token = jwtTokenProvider.generateToken(
            savedUser.getEmail(),
            savedUser.getId(),
            savedUser.getRole()
        );

        return AuthResponse.of(
            token,
            jwtTokenProvider.getExpirationMs(),
            savedUser.getId(),
            savedUser.getEmail(),
            savedUser.getRole(),
            savedUser.getSellerApproved()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, request.password())
        );

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepositoryPort.findByEmail(userPrincipal.email())
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userPrincipal.email()));

        String token = jwtTokenProvider.generateToken(userPrincipal);

        log.info("User logged in successfully: {}", email);

        return AuthResponse.of(
            token,
            jwtTokenProvider.getExpirationMs(),
            user.getId(),
            user.getEmail(),
            user.getRole(),
            user.getSellerApproved()
        );
    }
}

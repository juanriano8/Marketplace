package com.marketplace.api.modules.admin.application.service;

import com.marketplace.api.modules.product.application.dto.SellerVerificationRequest;
import com.marketplace.api.modules.user.application.dto.UserResponse;
import com.marketplace.api.modules.user.application.mapper.UserMapper;
import com.marketplace.api.modules.user.domain.model.User;
import com.marketplace.api.modules.user.domain.port.outbound.UserRepositoryPort;
import com.marketplace.api.shared.exception.DomainException;
import com.marketplace.api.shared.exception.ResourceNotFoundException;
import com.marketplace.api.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepositoryPort userRepositoryPort;
    private final UserMapper userMapper;

    /**
     * Lists seller accounts for the moderation queue.
     *
     * @param approved {@code true} for verified sellers, {@code false} for those still pending,
     *                 {@code null} for every seller
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> listSellers(Boolean approved, Pageable pageable) {
        return userRepositoryPort.findSellers(approved, pageable)
            .map(userMapper::toResponse);
    }

    @Transactional
    public UserResponse verifySeller(UUID sellerId, SellerVerificationRequest request) {
        User user = userRepositoryPort.findById(sellerId)
            .orElseThrow(() -> new ResourceNotFoundException("Seller not found with id: " + sellerId));

        if (user.getRole() != UserRole.ROLE_SELLER) {
            throw new DomainException("User with id " + sellerId + " is not a seller");
        }

        if (Boolean.TRUE.equals(request.approved())) {
            user.setSellerApproved(true);
            log.info("Admin approved seller id={}", sellerId);
        } else {
            user.setSellerApproved(false);
            user.setEnabled(false);
            log.info("Admin rejected seller id={}", sellerId);
        }

        User saved = userRepositoryPort.save(user);
        return userMapper.toResponse(saved);
    }
}

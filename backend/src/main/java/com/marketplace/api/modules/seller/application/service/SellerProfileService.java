package com.marketplace.api.modules.seller.application.service;

import com.marketplace.api.modules.seller.application.dto.SellerProfileResponse;
import com.marketplace.api.modules.seller.application.dto.UpdateSellerProfileRequest;
import com.marketplace.api.modules.seller.application.mapper.SellerProfileMapper;
import com.marketplace.api.modules.seller.domain.model.SellerProfile;
import com.marketplace.api.modules.seller.domain.port.inbound.SellerProfileUseCase;
import com.marketplace.api.modules.seller.domain.port.outbound.SellerProfileRepositoryPort;
import com.marketplace.api.modules.user.domain.model.User;
import com.marketplace.api.modules.user.domain.port.outbound.UserRepositoryPort;
import com.marketplace.api.shared.exception.DomainException;
import com.marketplace.api.shared.exception.ResourceNotFoundException;
import com.marketplace.api.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerProfileService implements SellerProfileUseCase {

    private final SellerProfileRepositoryPort sellerProfileRepositoryPort;
    private final SellerProfileMapper sellerProfileMapper;
    private final UserRepositoryPort userRepositoryPort;

    @Override
    @Transactional
    public SellerProfileResponse getPublicProfile(UUID sellerId) {
        return sellerProfileMapper.toResponse(getOrCreate(sellerId));
    }

    @Override
    @Transactional
    public SellerProfileResponse getMyProfile(UUID sellerId) {
        return sellerProfileMapper.toResponse(getOrCreate(sellerId));
    }

    @Override
    @Transactional
    public SellerProfileResponse updateMyProfile(UUID sellerId, UpdateSellerProfileRequest request) {
        SellerProfile profile = getOrCreate(sellerId);
        profile.update(request.storeName(), request.description(), request.logoUrl());
        return sellerProfileMapper.toResponse(sellerProfileRepositoryPort.save(profile));
    }

    /** Crea el perfil de forma perezosa para vendedores que no lo tenían. */
    private SellerProfile getOrCreate(UUID sellerId) {
        return sellerProfileRepositoryPort.findByUserId(sellerId).orElseGet(() -> {
            User user = userRepositoryPort.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found with id: " + sellerId));

            if (user.getRole() != UserRole.ROLE_SELLER) {
                throw new DomainException("User with id " + sellerId + " is not a seller");
            }

            String storeName = user.getEmail() != null && user.getEmail().contains("@")
                ? user.getEmail().substring(0, user.getEmail().indexOf('@'))
                : "Mi tienda";

            log.info("Creando perfil de vendedor para {}", sellerId);
            return sellerProfileRepositoryPort.save(new SellerProfile(sellerId, storeName));
        });
    }
}

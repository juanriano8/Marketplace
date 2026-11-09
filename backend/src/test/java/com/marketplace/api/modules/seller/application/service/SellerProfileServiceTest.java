package com.marketplace.api.modules.seller.application.service;

import com.marketplace.api.modules.seller.application.dto.SellerProfileResponse;
import com.marketplace.api.modules.seller.application.dto.UpdateSellerProfileRequest;
import com.marketplace.api.modules.seller.application.mapper.SellerProfileMapper;
import com.marketplace.api.modules.seller.domain.model.SellerProfile;
import com.marketplace.api.modules.seller.domain.port.outbound.SellerProfileRepositoryPort;
import com.marketplace.api.modules.user.domain.model.User;
import com.marketplace.api.modules.user.domain.port.outbound.UserRepositoryPort;
import com.marketplace.api.shared.security.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SellerProfileServiceTest {

    private SellerProfileRepositoryPort profiles;
    private SellerProfileMapper mapper;
    private UserRepositoryPort users;
    private SellerProfileService service;

    @BeforeEach
    void setUp() {
        profiles = mock(SellerProfileRepositoryPort.class);
        mapper = mock(SellerProfileMapper.class);
        users = mock(UserRepositoryPort.class);
        service = new SellerProfileService(profiles, mapper, users);
    }

    @Test
    void creaPerfilPerezosoConElNombreDelCorreo() {
        UUID sellerId = UUID.randomUUID();
        User seller = new User("tienda.demo@marketplace.com", "hash", UserRole.ROLE_SELLER);
        seller.setId(sellerId);

        SellerProfileResponse response = new SellerProfileResponse(
            sellerId, "tienda.demo", null, null, null, 0
        );

        when(profiles.findByUserId(sellerId)).thenReturn(Optional.empty());
        when(users.findById(sellerId)).thenReturn(Optional.of(seller));
        when(profiles.save(any(SellerProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toResponse(any(SellerProfile.class))).thenReturn(response);

        SellerProfileResponse result = service.getPublicProfile(sellerId);

        assertThat(result).isSameAs(response);
        verify(profiles).save(any(SellerProfile.class));
    }

    @Test
    void actualizaElPerfilExistente() {
        UUID sellerId = UUID.randomUUID();
        SellerProfile profile = new SellerProfile(sellerId, "Tienda");

        when(profiles.findByUserId(sellerId)).thenReturn(Optional.of(profile));
        when(profiles.save(profile)).thenReturn(profile);
        when(mapper.toResponse(profile)).thenReturn(
            new SellerProfileResponse(sellerId, "Nueva tienda", "Desc", null, null, 0)
        );

        service.updateMyProfile(sellerId, new UpdateSellerProfileRequest("Nueva tienda", "Desc", null));

        assertThat(profile.getStoreName()).isEqualTo("Nueva tienda");
        assertThat(profile.getDescription()).isEqualTo("Desc");
        verify(profiles).save(profile);
    }
}

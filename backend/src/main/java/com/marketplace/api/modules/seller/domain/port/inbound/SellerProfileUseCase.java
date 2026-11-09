package com.marketplace.api.modules.seller.domain.port.inbound;

import com.marketplace.api.modules.seller.application.dto.SellerProfileResponse;
import com.marketplace.api.modules.seller.application.dto.UpdateSellerProfileRequest;

import java.util.UUID;

public interface SellerProfileUseCase {

    SellerProfileResponse getPublicProfile(UUID sellerId);

    SellerProfileResponse getMyProfile(UUID sellerId);

    SellerProfileResponse updateMyProfile(UUID sellerId, UpdateSellerProfileRequest request);
}

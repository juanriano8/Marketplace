package com.marketplace.api.modules.seller.application.mapper;

import com.marketplace.api.modules.seller.application.dto.SellerProfileResponse;
import com.marketplace.api.modules.seller.domain.model.SellerProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SellerProfileMapper {

    @Mapping(source = "userId", target = "sellerId")
    SellerProfileResponse toResponse(SellerProfile profile);
}

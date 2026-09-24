package com.marketplace.api.modules.product.application.mapper;

import com.marketplace.api.modules.product.application.dto.ProductResponse;
import com.marketplace.api.modules.product.domain.model.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductResponse toResponse(Product product);
}

package com.marketplace.api.modules.product.domain.port.inbound;

import com.marketplace.api.modules.product.application.dto.CreateProductRequest;
import com.marketplace.api.modules.product.application.dto.ProductApprovalRequest;
import com.marketplace.api.modules.product.application.dto.ProductResponse;
import com.marketplace.api.modules.product.application.dto.UpdateProductRequest;
import com.marketplace.api.modules.product.domain.model.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ProductUseCase {
    // Public
    Page<ProductResponse> listActiveProducts(Pageable pageable);
    ProductResponse getProductBySlug(String slug);
    ProductResponse getProductById(UUID id);
    List<ProductResponse> listRankedProducts(int limit);

    // Seller
    ProductResponse createProduct(CreateProductRequest request, UUID sellerId);
    ProductResponse updateProduct(UUID productId, UpdateProductRequest request, UUID sellerId);
    Page<ProductResponse> listSellerProducts(UUID sellerId, Pageable pageable);

    // Admin
    ProductResponse approveOrRejectProduct(UUID productId, ProductApprovalRequest request);
    Page<ProductResponse> listProductsByStatus(ProductStatus status, Pageable pageable);
}

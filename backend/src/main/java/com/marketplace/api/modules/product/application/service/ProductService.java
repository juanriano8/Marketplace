package com.marketplace.api.modules.product.application.service;

import com.marketplace.api.modules.inventory.domain.port.inbound.InventoryUseCase;
import com.marketplace.api.modules.inventory.domain.port.inbound.StockQueryPort;
import com.marketplace.api.modules.product.application.dto.CreateProductRequest;
import com.marketplace.api.modules.product.application.dto.ProductApprovalRequest;
import com.marketplace.api.modules.product.application.dto.ProductResponse;
import com.marketplace.api.modules.product.application.dto.UpdateProductRequest;
import com.marketplace.api.modules.product.application.mapper.ProductMapper;
import com.marketplace.api.modules.product.domain.model.Product;
import com.marketplace.api.modules.product.domain.model.ProductStatus;
import com.marketplace.api.modules.product.domain.port.inbound.ProductUseCase;
import com.marketplace.api.modules.product.domain.port.outbound.ProductRepositoryPort;
import com.marketplace.api.modules.user.domain.port.outbound.UserRepositoryPort;
import com.marketplace.api.shared.exception.DomainException;
import com.marketplace.api.shared.exception.ResourceNotFoundException;
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
public class ProductService implements ProductUseCase {

    private final ProductRepositoryPort productRepositoryPort;
    private final ProductMapper productMapper;
    private final InventoryUseCase inventoryUseCase;
    private final StockQueryPort stockQueryPort;
    private final UserRepositoryPort userRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> listActiveProducts(Pageable pageable) {
        return productRepositoryPort.findAllActive(pageable)
            .map(this::toResponseWithLiveStock);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductBySlug(String slug) {
        Product product = productRepositoryPort.findBySlug(slug)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with slug: " + slug));

        if (!product.isActive()) {
            throw new ResourceNotFoundException("Product not found with slug: " + slug);
        }
        return toResponseWithLiveStock(product);
    }

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request, UUID sellerId) {
        // Only sellers verified by an administrator may publish to the catalog.
        if (!userRepositoryPort.isSellerApproved(sellerId)) {
            throw new DomainException("Your seller account is not verified yet; an administrator must approve it first");
        }

        Product product = new Product(
            request.name(),
            request.description(),
            request.price(),
            request.currencyCode() != null ? request.currencyCode() : "USD",
            request.stockQuantity(),
            sellerId,
            request.category()
        );
        if (request.imageUrl() != null) {
            product.setImageUrl(request.imageUrl());
        }

        Product saved = productRepositoryPort.save(product);

        // The inventory module becomes the authoritative stock ledger from this point on.
        inventoryUseCase.initialiseStock(saved.getId(), sellerId, request.stockQuantity());

        log.info("Product created by seller {}: id={}, slug={}", sellerId, saved.getId(), saved.getSlug());
        return toResponseWithLiveStock(saved);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(UUID productId, UpdateProductRequest request, UUID sellerId) {
        Product product = productRepositoryPort.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        // BOLA check — seller can only update their own products
        if (!product.isOwnedBy(sellerId)) {
            throw new DomainException("You are not authorized to update this product");
        }

        if (request.name() != null) product.setName(request.name());
        if (request.description() != null) product.setDescription(request.description());
        if (request.price() != null) product.setPrice(request.price());
        if (request.category() != null) product.setCategory(request.category());
        if (request.imageUrl() != null) product.setImageUrl(request.imageUrl());

        // Stock is intentionally NOT updated here: from product creation onwards the inventory
        // module owns physical stock, adjusted through POST /api/v1/seller/inventory/adjust.
        Product updated = productRepositoryPort.save(product);

        log.info("Product {} updated by seller {}", productId, sellerId);
        return toResponseWithLiveStock(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> listSellerProducts(UUID sellerId, Pageable pageable) {
        return productRepositoryPort.findBySellerId(sellerId, pageable)
            .map(this::toResponseWithLiveStock);
    }

    @Override
    @Transactional
    public ProductResponse approveOrRejectProduct(UUID productId, ProductApprovalRequest request) {
        Product product = productRepositoryPort.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (Boolean.TRUE.equals(request.approved())) {
            product.approve();
            log.info("Admin approved product id={}", productId);
        } else {
            product.reject();
            log.info("Admin rejected product id={}", productId);
        }

        return toResponseWithLiveStock(productRepositoryPort.save(product));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> listProductsByStatus(ProductStatus status, Pageable pageable) {
        return productRepositoryPort.findByStatus(status, pageable)
            .map(this::toResponseWithLiveStock);
    }

    /**
     * Enriches the stored product with the live sellable quantity from the inventory ledger.
     *
     * <p>The {@code stock_quantity} column on the product is the declaration made at creation time;
     * the inventory module is authoritative afterwards, so the DTO must not report a stale value.
     * When no stock record exists yet (legacy rows) the stored value is returned.</p>
     */
    private ProductResponse toResponseWithLiveStock(Product product) {
        ProductResponse response = productMapper.toResponse(product);

        return stockQueryPort.findAvailability(product.getId())
            .map(availability -> new ProductResponse(
                response.id(),
                response.name(),
                response.description(),
                response.slug(),
                response.price(),
                response.currencyCode(),
                availability.sellableQuantity(),
                response.imageUrl(),
                response.status(),
                response.sellerId(),
                response.category(),
                response.createdAt(),
                response.updatedAt()
            ))
            .orElseGet(() -> {
                log.debug("No stock record yet for product {}; reporting the declared quantity", product.getId());
                return response;
            });
    }
}

package com.marketplace.api.modules.product.application.service;

import com.marketplace.api.modules.product.application.dto.CreateProductRequest;
import com.marketplace.api.modules.product.application.dto.ProductApprovalRequest;
import com.marketplace.api.modules.product.application.dto.ProductResponse;
import com.marketplace.api.modules.product.application.dto.UpdateProductRequest;
import com.marketplace.api.modules.product.application.mapper.ProductMapper;
import com.marketplace.api.modules.product.domain.model.Product;
import com.marketplace.api.modules.product.domain.port.inbound.ProductUseCase;
import com.marketplace.api.modules.product.domain.port.outbound.ProductRepositoryPort;
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

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> listActiveProducts(Pageable pageable) {
        return productRepositoryPort.findAllActive(pageable)
            .map(productMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductBySlug(String slug) {
        Product product = productRepositoryPort.findBySlug(slug)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with slug: " + slug));

        if (!product.isActive()) {
            throw new ResourceNotFoundException("Product not found with slug: " + slug);
        }
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request, UUID sellerId) {
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
        log.info("Product created by seller {}: id={}, slug={}", sellerId, saved.getId(), saved.getSlug());
        return productMapper.toResponse(saved);
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
        if (request.stockQuantity() != null) product.setStockQuantity(request.stockQuantity());
        if (request.category() != null) product.setCategory(request.category());
        if (request.imageUrl() != null) product.setImageUrl(request.imageUrl());

        Product updated = productRepositoryPort.save(product);
        log.info("Product {} updated by seller {}", productId, sellerId);
        return productMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> listSellerProducts(UUID sellerId, Pageable pageable) {
        return productRepositoryPort.findBySellerId(sellerId, pageable)
            .map(productMapper::toResponse);
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

        return productMapper.toResponse(productRepositoryPort.save(product));
    }
}

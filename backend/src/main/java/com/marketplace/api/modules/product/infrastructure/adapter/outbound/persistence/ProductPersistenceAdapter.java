package com.marketplace.api.modules.product.infrastructure.adapter.outbound.persistence;

import com.marketplace.api.modules.product.domain.model.Product;
import com.marketplace.api.modules.product.domain.model.ProductStatus;
import com.marketplace.api.modules.product.domain.port.outbound.ProductRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductPersistenceAdapter implements ProductRepositoryPort {

    private final SpringDataProductRepository springDataProductRepository;

    @Override
    public Product save(Product product) {
        return springDataProductRepository.save(product);
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return springDataProductRepository.findById(id);
    }

    @Override
    public Optional<Product> findBySlug(String slug) {
        return springDataProductRepository.findBySlug(slug);
    }

    @Override
    public Page<Product> findAllActive(Pageable pageable) {
        return springDataProductRepository.findByStatus(ProductStatus.ACTIVE, pageable);
    }

    @Override
    public Page<Product> findBySellerIdAndStatus(UUID sellerId, ProductStatus status, Pageable pageable) {
        return springDataProductRepository.findBySellerIdAndStatus(sellerId, status, pageable);
    }

    @Override
    public Page<Product> findBySellerId(UUID sellerId, Pageable pageable) {
        return springDataProductRepository.findBySellerId(sellerId, pageable);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return springDataProductRepository.existsBySlug(slug);
    }
}

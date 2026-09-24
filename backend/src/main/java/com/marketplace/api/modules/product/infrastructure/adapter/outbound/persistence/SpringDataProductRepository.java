package com.marketplace.api.modules.product.infrastructure.adapter.outbound.persistence;

import com.marketplace.api.modules.product.domain.model.Product;
import com.marketplace.api.modules.product.domain.model.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataProductRepository extends JpaRepository<Product, UUID> {
    Optional<Product> findBySlug(String slug);
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);
    Page<Product> findBySellerIdAndStatus(UUID sellerId, ProductStatus status, Pageable pageable);
    Page<Product> findBySellerId(UUID sellerId, Pageable pageable);
    boolean existsBySlug(String slug);
}

package com.marketplace.api.modules.product.domain.port.outbound;

import com.marketplace.api.modules.product.domain.model.Product;
import com.marketplace.api.modules.product.domain.model.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepositoryPort {
    Product save(Product product);
    Optional<Product> findById(UUID id);
    Optional<Product> findBySlug(String slug);
    Page<Product> findAllActive(Pageable pageable);

    /** Productos activos ordenados por rating y decaimiento temporal. */
    List<Product> findRankedActive(int limit);
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);
    Page<Product> findBySellerIdAndStatus(UUID sellerId, ProductStatus status, Pageable pageable);
    Page<Product> findBySellerId(UUID sellerId, Pageable pageable);
    boolean existsBySlug(String slug);
}

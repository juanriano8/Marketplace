package com.marketplace.api.modules.product.domain.model;

import com.marketplace.api.shared.domain.BaseEntity;
import com.marketplace.api.shared.exception.DomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "products")
public class Product extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "slug", unique = true, nullable = false)
    private String slug;

    @Column(name = "price", nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Column(name = "image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProductStatus status;

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    @Column(name = "category")
    private String category;

    public Product(String name, String description, BigDecimal price,
                   String currencyCode, Integer stockQuantity, UUID sellerId, String category) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("Product price must be greater than zero");
        }
        if (stockQuantity == null || stockQuantity < 0) {
            throw new DomainException("Stock quantity cannot be negative");
        }
        this.name = name;
        this.description = description;
        this.price = price;
        this.currencyCode = currencyCode != null ? currencyCode : "USD";
        this.stockQuantity = stockQuantity;
        this.sellerId = sellerId;
        this.category = category;
        this.status = ProductStatus.PENDING_APPROVAL;
        this.slug = generateSlug(name);
    }

    public void approve() {
        if (this.status != ProductStatus.PENDING_APPROVAL) {
            throw new DomainException("Only products in PENDING_APPROVAL can be approved");
        }
        this.status = ProductStatus.ACTIVE;
    }

    public void reject() {
        if (this.status != ProductStatus.PENDING_APPROVAL) {
            throw new DomainException("Only products in PENDING_APPROVAL can be rejected");
        }
        this.status = ProductStatus.REJECTED;
    }

    public void deactivate() {
        this.status = ProductStatus.INACTIVE;
    }

    public boolean isActive() {
        return ProductStatus.ACTIVE == this.status;
    }

    public boolean isOwnedBy(UUID sellerId) {
        return this.sellerId != null && this.sellerId.equals(sellerId);
    }

    private static String generateSlug(String name) {
        return name.toLowerCase()
            .replaceAll("[^a-z0-9\\s-]", "")
            .replaceAll("\\s+", "-")
            .replaceAll("-+", "-")
            .strip()
            + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}

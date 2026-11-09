package com.marketplace.api.modules.favorite.domain.model;

import com.marketplace.api.shared.domain.BaseEntity;
import com.marketplace.api.shared.exception.DomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/** Producto guardado por un comprador. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "favoritos",
    uniqueConstraints = @UniqueConstraint(
        name = "favoritos_user_product_unique",
        columnNames = {"user_id", "product_id"}
    )
)
public class Favorite extends BaseEntity {

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "product_id", nullable = false, updatable = false)
    private UUID productId;

    public Favorite(UUID userId, UUID productId) {
        if (userId == null || productId == null) {
            throw new DomainException("A favorite requires a user and a product");
        }
        this.userId = userId;
        this.productId = productId;
    }
}

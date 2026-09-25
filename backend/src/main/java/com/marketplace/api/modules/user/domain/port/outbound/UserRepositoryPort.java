package com.marketplace.api.modules.user.domain.port.outbound;

import com.marketplace.api.modules.user.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {
    User save(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    /** Guards seller-only operations that require a verified (admin approved) account. */
    boolean isSellerApproved(UUID sellerId);

    /**
     * Administrative listing of seller accounts.
     *
     * @param approved {@code true} for verified sellers, {@code false} for pending ones,
     *                 {@code null} to list every seller
     */
    Page<User> findSellers(Boolean approved, Pageable pageable);
}

package com.marketplace.api.modules.user.domain.port.outbound;

import com.marketplace.api.modules.user.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {
    User save(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    /** Guards seller-only operations that require a verified (admin approved) account. */
    boolean isSellerApproved(UUID sellerId);
}

package com.marketplace.api.modules.user.infrastructure.adapter.outbound.persistence;

import com.marketplace.api.modules.user.domain.model.User;
import com.marketplace.api.modules.user.domain.port.outbound.UserRepositoryPort;
import com.marketplace.api.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository springDataUserRepository;

    @Override
    public User save(User user) {
        return springDataUserRepository.save(user);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return springDataUserRepository.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springDataUserRepository.findByEmail(email);
    }

    @Override
    public boolean existsByEmail(String email) {
        return springDataUserRepository.existsByEmail(email);
    }

    @Override
    public boolean isSellerApproved(UUID sellerId) {
        return springDataUserRepository.findById(sellerId)
            .filter(User::isEnabled)
            .map(user -> Boolean.TRUE.equals(user.getSellerApproved()))
            .orElse(false);
    }

    @Override
    public Page<User> findSellers(Boolean approved, Pageable pageable) {
        // Con `approved == null` hay que usar una consulta sin filtro: un `= null` en SQL nunca
        // coincide y devolvería una lista vacía.
        return approved == null
            ? springDataUserRepository.findByRole(UserRole.ROLE_SELLER, pageable)
            : springDataUserRepository.findByRoleAndSellerApproved(UserRole.ROLE_SELLER, approved, pageable);
    }
}

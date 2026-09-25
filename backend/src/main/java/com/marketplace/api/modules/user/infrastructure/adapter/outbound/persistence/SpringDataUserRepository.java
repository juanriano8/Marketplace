package com.marketplace.api.modules.user.infrastructure.adapter.outbound.persistence;

import com.marketplace.api.modules.user.domain.model.User;
import com.marketplace.api.shared.security.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataUserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    /** Bandeja de verificación del administrador: vendedores aprobados o pendientes. */
    Page<User> findByRoleAndSellerApproved(UserRole role, Boolean sellerApproved, Pageable pageable);

    /** Todos los vendedores, sin filtrar por estado de verificación. */
    Page<User> findByRole(UserRole role, Pageable pageable);
}

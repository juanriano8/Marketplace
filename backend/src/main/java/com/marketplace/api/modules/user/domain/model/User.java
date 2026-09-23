package com.marketplace.api.modules.user.domain.model;

import com.marketplace.api.shared.domain.BaseEntity;
import com.marketplace.api.shared.security.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    // Campos específicos para vendedores (opcionales)
    @Column(name = "seller_approved")
    private Boolean sellerApproved;
    
    public User(String email, String password, UserRole role) {
        this.email = email;
        this.password = password;
        this.role = role;
        
        if (role == UserRole.ROLE_SELLER) {
            this.sellerApproved = false; // Requiere moderación
        }
    }
}

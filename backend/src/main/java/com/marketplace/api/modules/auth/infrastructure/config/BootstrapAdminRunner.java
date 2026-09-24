package com.marketplace.api.modules.auth.infrastructure.config;

import com.marketplace.api.modules.user.domain.model.User;
import com.marketplace.api.modules.user.domain.port.outbound.UserRepositoryPort;
import com.marketplace.api.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates the first administrator account on startup when it does not exist yet.
 *
 * <p>Needed because {@code POST /api/v1/auth/register/buyer} and {@code .../seller} are the only public
 * registration endpoints, so without a seeded administrator there is no way to call the
 * {@code /api/v1/admin/**} endpoints. The password is hashed with the application's own
 * {@link PasswordEncoder}, which guarantees a hash the login endpoint accepts.</p>
 *
 * <p>Disabled unless {@code BOOTSTRAP_ADMIN_EMAIL} and {@code BOOTSTRAP_ADMIN_PASSWORD} are set, so it
 * is inert in production; when deploying for real, provision the administrator manually or through
 * GCP Secret Manager and leave this off.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BootstrapAdminRunner implements ApplicationRunner {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin.email:}")
    private String adminEmail;

    @Value("${app.bootstrap.admin.password:}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) {
        if (adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.isBlank()) {
            log.info("Bootstrap admin disabled (BOOTSTRAP_ADMIN_EMAIL / BOOTSTRAP_ADMIN_PASSWORD not set)");
            return;
        }

        String email = adminEmail.trim().toLowerCase();

        if (userRepositoryPort.existsByEmail(email)) {
            log.info("Bootstrap admin already present for {}", email);
            return;
        }

        User admin = new User(email, passwordEncoder.encode(adminPassword), UserRole.ROLE_ADMIN);
        admin.setEnabled(true);
        userRepositoryPort.save(admin);

        log.warn("Bootstrap administrator created for {} — change this password before going live", email);
    }
}

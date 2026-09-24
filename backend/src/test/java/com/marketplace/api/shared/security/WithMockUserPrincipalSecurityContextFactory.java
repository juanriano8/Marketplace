package com.marketplace.api.shared.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.List;
import java.util.UUID;

/**
 * Builds a {@link SecurityContext} holding a {@link UserPrincipal} for the given role, so MVC tests
 * can exercise the JWT-shaped principal without generating real tokens.
 */
public class WithMockUserPrincipalSecurityContextFactory
    implements WithSecurityContextFactory<WithMockUserPrincipal> {

    @Override
    public SecurityContext createSecurityContext(WithMockUserPrincipal annotation) {
        UUID userId = annotation.id().isBlank() ? UUID.randomUUID() : UUID.fromString(annotation.id());

        UserPrincipal principal = new UserPrincipal(
            userId,
            annotation.email(),
            "encoded-password",
            annotation.role(),
            annotation.enabled()
        );

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            principal,
            null,
            List.of(new SimpleGrantedAuthority(annotation.role().name()))
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        return context;
    }
}

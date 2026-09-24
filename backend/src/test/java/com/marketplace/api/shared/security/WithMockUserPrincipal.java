package com.marketplace.api.shared.security;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Populates the security context with a {@link UserPrincipal} of the given {@link UserRole}.
 *
 * <p>Spring Security's {@code @WithMockUser} cannot be used here because the application code reads
 * the principal as a {@link UserPrincipal} record; this annotation produces that exact type.</p>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockUserPrincipalSecurityContextFactory.class)
public @interface WithMockUserPrincipal {

    UserRole role() default UserRole.ROLE_BUYER;

    String email() default "principal@marketplace.test";

    /** Empty means a random UUID is generated for each test. */
    String id() default "";

    boolean enabled() default true;
}

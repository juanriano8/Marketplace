package com.marketplace.api.shared.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", secret);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", 3600000L);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtRefreshExpirationMs", 7200000L);
    }

    @Test
    @DisplayName("Should generate valid token and extract claims accurately")
    void shouldGenerateAndExtractClaims() {
        UUID userId = UUID.randomUUID();
        UserPrincipal principal = new UserPrincipal(
            userId,
            "test@marketplace.com",
            "encodedPassword",
            UserRole.ROLE_BUYER,
            true
        );

        String token = jwtTokenProvider.generateToken(principal);

        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.extractUsername(token)).isEqualTo("test@marketplace.com");
        assertThat(jwtTokenProvider.extractRole(token)).isEqualTo(UserRole.ROLE_BUYER.name());
        assertThat(jwtTokenProvider.extractUserId(token)).isEqualTo(userId);
    }

    @Test
    @DisplayName("Should reject tampered or invalid token")
    void shouldRejectInvalidToken() {
        assertThat(jwtTokenProvider.validateToken("invalid.jwt.token")).isFalse();
    }
}

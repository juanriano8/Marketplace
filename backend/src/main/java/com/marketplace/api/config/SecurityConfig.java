package com.marketplace.api.config;

import com.marketplace.api.shared.security.JwtAccessDeniedHandler;
import com.marketplace.api.shared.security.JwtAuthenticationEntryPoint;
import com.marketplace.api.shared.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Stateless JWT security configuration.
 *
 * <p>Authorization is expressed twice on purpose: coarse URL rules here (defence in depth, and they
 * keep unauthenticated traffic out before it reaches a controller) and fine-grained
 * {@code @PreAuthorize} role checks plus ownership (BOLA) validations in the services. The most
 * specific matchers must come first because Spring Security applies them in declaration order.</p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String ADMIN = "ADMIN";
    private static final String SELLER = "SELLER";
    private static final String BUYER = "BUYER";

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; frame-ancestors 'none'; sandbox"))
                .frameOptions(frameOptions -> frameOptions.deny())
                .xssProtection(xss -> xss.disable()) // Deshabilitado en favor de CSP
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // ---- Infrastructure / documentation (public) ----
                .requestMatchers(
                    "/api/v1/auth/**",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/actuator/health"
                ).permitAll()

                // ---- Public catalog ----
                .requestMatchers(HttpMethod.GET, "/api/v1/products", "/api/v1/products/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/inventory/check/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/reviews/product/**").permitAll()

                // ---- Administrator ----
                .requestMatchers("/api/v1/admin/**").hasRole(ADMIN)

                // ---- Seller ----
                .requestMatchers("/api/v1/seller/**").hasRole(SELLER)

                // ---- Buyer ----
                // Checkout lives under /api/v1/orders because it is the order-creating operation.
                .requestMatchers(HttpMethod.POST, "/api/v1/orders/checkout").hasRole(BUYER)
                .requestMatchers("/api/v1/cart/**").hasRole(BUYER)
                .requestMatchers("/api/v1/buyer/**").hasRole(BUYER)

                // Anything not listed above (including future endpoints) requires an account.
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

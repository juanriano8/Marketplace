package com.marketplace.api.config;

import com.marketplace.api.shared.security.JwtAccessDeniedHandler;
import com.marketplace.api.shared.security.JwtAuthenticationEntryPoint;
import com.marketplace.api.shared.security.JwtAuthenticationFilter;
import com.marketplace.api.shared.ratelimit.RateLimitFilter;
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
    private final RateLimitFilter rateLimitFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .headers(headers -> headers
                // NOTA: se quito la directiva "sandbox" porque, al no incluir "allow-scripts",
                // el navegador bloquea TODO el JavaScript de la pagina y Swagger UI
                // (/swagger-ui.html) se quedaba en blanco. El resto de la CSP se mantiene.
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; frame-ancestors 'none'"))
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
                .requestMatchers(HttpMethod.GET, "/api/v1/sellers/*/profile").permitAll()

                // ---- Administrator ----
                .requestMatchers("/api/v1/admin/**").hasRole(ADMIN)

                // ---- Seller ----
                .requestMatchers("/api/v1/seller/**").hasRole(SELLER)

                // ---- Buyer ----
                // Checkout lives under /api/v1/orders because it is the order-creating operation.
                .requestMatchers(HttpMethod.POST, "/api/v1/orders/checkout").hasRole(BUYER)
                .requestMatchers("/api/v1/cart/**").hasRole(BUYER)
                .requestMatchers("/api/v1/buyer/**").hasRole(BUYER)
                .requestMatchers("/api/v1/favorites/**").hasRole(BUYER)

                // Anything not listed above (including future endpoints) requires an account.
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            // El limitador va DESPUES del filtro JWT: necesita la identidad ya resuelta para
            // elegir el cubo (anonimo por IP, autenticado por usuario, auth por IP).
            .addFilterAfter(rateLimitFilter, JwtAuthenticationFilter.class);

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

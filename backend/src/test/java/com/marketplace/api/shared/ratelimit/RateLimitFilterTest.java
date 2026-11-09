package com.marketplace.api.shared.ratelimit;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas del limitador de tasa. No necesitan base de datos: el filtro solo depende de la peticion
 * y de la configuracion.
 */
class RateLimitFilterTest {

    private final RateLimitProperties properties = new RateLimitProperties();
    private final RateLimitFilter filter = new RateLimitFilter(properties);

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    private MockHttpServletResponse ejecutar(RateLimitFilter filtro, String ruta) throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", ruta);
        request.setRemoteAddr("10.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filtro.doFilter(request, response, new MockFilterChain());
        return response;
    }

    @Test
    @DisplayName("las peticiones por encima del limite reciben 429 con Retry-After")
    void bloqueaAlSuperarElLimite() throws Exception {
        properties.setEnabled(true);
        properties.setAnonymousPerMinute(2);

        assertThat(ejecutar(filter, "/api/v1/products").getStatus()).isEqualTo(200);
        assertThat(ejecutar(filter, "/api/v1/products").getStatus()).isEqualTo(200);

        MockHttpServletResponse tercera = ejecutar(filter, "/api/v1/products");
        assertThat(tercera.getStatus()).isEqualTo(429);
        assertThat(tercera.getHeader("Retry-After")).isEqualTo("60");
        assertThat(tercera.getContentType()).contains("application/problem+json");
        assertThat(tercera.getContentAsString()).contains("rate-limit-exceeded");
    }

    @Test
    @DisplayName("cada IP tiene su propio cubo")
    void separaPorIp() throws Exception {
        properties.setEnabled(true);
        properties.setAnonymousPerMinute(1);

        MockHttpServletRequest uno = new MockHttpServletRequest("GET", "/api/v1/products");
        uno.setRemoteAddr("10.0.0.1");
        assertThat(doFilter(uno).getStatus()).isEqualTo(200);

        MockHttpServletRequest otro = new MockHttpServletRequest("GET", "/api/v1/products");
        otro.setRemoteAddr("10.0.0.2");
        assertThat(doFilter(otro).getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("usa X-Forwarded-For cuando viene del balanceador")
    void usaCabeceraForwarded() throws Exception {
        properties.setEnabled(true);
        properties.setAnonymousPerMinute(1);

        MockHttpServletRequest primera = new MockHttpServletRequest("GET", "/api/v1/products");
        primera.setRemoteAddr("10.0.0.9"); // IP del proxy
        primera.addHeader("X-Forwarded-For", "203.0.113.7, 10.0.0.9");
        assertThat(doFilter(primera).getStatus()).isEqualTo(200);

        // Otra IP real detras del mismo proxy no debe quedar bloqueada
        MockHttpServletRequest otra = new MockHttpServletRequest("GET", "/api/v1/products");
        otra.setRemoteAddr("10.0.0.9");
        otra.addHeader("X-Forwarded-For", "203.0.113.8");
        assertThat(doFilter(otra).getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("health y swagger nunca se limitan")
    void excluyeInfraestructura() throws Exception {
        properties.setEnabled(true);
        properties.setAnonymousPerMinute(1);

        for (int i = 0; i < 5; i++) {
            assertThat(ejecutar(filter, "/actuator/health").getStatus()).isEqualTo(200);
            assertThat(ejecutar(filter, "/v3/api-docs").getStatus()).isEqualTo(200);
        }
    }

    @Test
    @DisplayName("autenticado usa su propio limite, no el anonimo")
    void autenticadoTieneOtroCubo() throws Exception {
        properties.setEnabled(true);
        properties.setAnonymousPerMinute(1);
        properties.setAuthenticatedPerMinute(5);

        assertThat(ejecutar(filter, "/api/v1/cart").getStatus()).isEqualTo(200);

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("buyer-1", null,
                List.of(new SimpleGrantedAuthority("ROLE_BUYER"))));

        // El cubo anonimo ya esta agotado, pero el de usuario no
        for (int i = 0; i < 5; i++) {
            assertThat(ejecutar(filter, "/api/v1/cart").getStatus()).isEqualTo(200);
        }
        assertThat(ejecutar(filter, "/api/v1/cart").getStatus()).isEqualTo(429);
    }

    @Test
    @DisplayName("login tiene limite estricto por IP")
    void loginTieneLimiteEstricto() throws Exception {
        properties.setEnabled(true);
        properties.setAuthPerMinute(2);

        assertThat(ejecutar(filter, "/api/v1/auth/login").getStatus()).isEqualTo(200);
        assertThat(ejecutar(filter, "/api/v1/auth/login").getStatus()).isEqualTo(200);
        assertThat(ejecutar(filter, "/api/v1/auth/login").getStatus()).isEqualTo(429);
    }

    @Test
    @DisplayName("desactivado deja pasar todo")
    void desactivadoNoLimita() throws Exception {
        properties.setEnabled(false);
        properties.setAnonymousPerMinute(1);

        for (int i = 0; i < 5; i++) {
            assertThat(ejecutar(filter, "/api/v1/products").getStatus()).isEqualTo(200);
        }
    }

    private MockHttpServletResponse doFilter(MockHttpServletRequest request) throws ServletException, IOException {
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }
}

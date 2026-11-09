package com.marketplace.api.shared.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Control de tasa por cliente usando Bucket4j.
 *
 * <p>Hay tres niveles, cada uno con su propio cubo:</p>
 * <ul>
 *   <li><b>auth</b>: login y registro, contados por IP. Protege contra fuerza bruta y abuso de
 *       creación de cuentas.</li>
 *   <li><b>user</b>: peticiones de un usuario autenticado, contadas por identidad. Es el límite más
 *       alto porque el usuario ya está identificado.</li>
 *   <li><b>anon</b>: catálogo y demás tráfico sin sesión, contado por IP.</li>
 * </ul>
 *
 * <p>El filtro se ejecuta <b>después</b> de {@code JwtAuthenticationFilter}, así que la identidad ya
 * está en el {@code SecurityContext} cuando se decide a qué cubo va la petición. La respuesta al
 * exceso es un {@code 429} con formato RFC 7807 y cabecera {@code Retry-After}.</p>
 *
 * <p>Los cubos viven en memoria del proceso. Con varias instancias de Cloud Run el límite es por
 * instancia; para un límite global haría falta Redis (Memorystore), que es la evolución natural del
 * módulo. Se documenta aquí para que la limitación sea explícita y no una sorpresa.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    /** Rutas de infraestructura que nunca se limitan (health, documentación). */
    private static final List<String> EXCLUDED_PREFIXES = List.of(
        "/actuator",
        "/swagger-ui",
        "/v3/api-docs"
    );

    /** Rutas con límite estricto por IP. */
    private static final List<String> AUTH_PATHS = List.of(
        "/api/v1/auth/login",
        "/api/v1/auth/register/buyer",
        "/api/v1/auth/register/seller"
    );

    private final RateLimitProperties properties;

    /** Un cubo por combinación de nivel + identidad. */
    private final Map<String, Entry> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (!properties.isEnabled() || isExcluded(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String tier = resolveTier(request);
        String key = tier + ':' + resolveIdentity(request, tier);
        long limit = limitFor(tier);

        Bucket bucket = resolveBucket(key, limit);

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
            return;
        }

        log.warn("Limite de tasa superado en {} (nivel {}, limite {}/min)", request.getRequestURI(), tier, limit);
        writeTooManyRequests(response, limit);
    }

    private boolean isExcluded(HttpServletRequest request) {
        String path = request.getRequestURI();
        for (String prefix : EXCLUDED_PREFIXES) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private String resolveTier(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (AUTH_PATHS.stream().anyMatch(path::startsWith)) {
            return "auth";
        }
        return isAuthenticated() ? "user" : "anon";
    }

    private boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
            && authentication.isAuthenticated()
            && !(authentication instanceof AnonymousAuthenticationToken);
    }

    private String resolveIdentity(HttpServletRequest request, String tier) {
        if ("user".equals(tier)) {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        }
        return clientIp(request);
    }

    /**
     * IP del cliente. Se tiene en cuenta {@code X-Forwarded-For} porque detrás del balanceador de
     * Cloud Run la IP de la conexión es la del proxy, no la del usuario.
     */
    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private long limitFor(String tier) {
        return switch (tier) {
            case "auth" -> properties.getAuthPerMinute();
            case "user" -> properties.getAuthenticatedPerMinute();
            default -> properties.getAnonymousPerMinute();
        };
    }

    private Bucket resolveBucket(String key, long limit) {
        evictIfNeeded();

        Entry entry = buckets.compute(key, (k, current) -> {
            if (current == null) {
                Bucket bucket = Bucket.builder()
                    .addLimit(Bandwidth.builder()
                        .capacity(limit)
                        .refillGreedy(limit, Duration.ofMinutes(1))
                        .build())
                    .build();
                return new Entry(bucket);
            }
            current.touch();
            return current;
        });

        return entry.bucket;
    }

    /** Purga cubos inactivos cuando el mapa crece demasiado; evita fugas de memoria. */
    private void evictIfNeeded() {
        if (buckets.size() < properties.getMaxTrackedKeys()) {
            return;
        }
        long threshold = System.currentTimeMillis() - Duration.ofMinutes(properties.getIdleMinutes()).toMillis();
        Iterator<Map.Entry<String, Entry>> iterator = buckets.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue().lastAccess < threshold) {
                iterator.remove();
            }
        }
    }

    /**
     * Respuesta de error en RFC 7807. Se escribe a mano para no arrastrar el {@code ObjectMapper}
     * dentro del filtro; el cuerpo es fijo y no interpola datos del cliente.
     */
    private void writeTooManyRequests(HttpServletResponse response, long limit) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/problem+json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Retry-After", "60");
        response.getWriter().write("""
            {"type":"https://api.marketplace.com/errors/rate-limit-exceeded",\
            "title":"Too Many Requests",\
            "status":429,\
            "detail":"Has superado el limite de %d peticiones por minuto. Vuelve a intentarlo en unos segundos."}\
            """.formatted(limit));
    }

    /** Cubo con marca de último acceso, para poder purgar los inactivos. */
    private static final class Entry {
        private final Bucket bucket;
        private volatile long lastAccess = System.currentTimeMillis();

        private Entry(Bucket bucket) {
            this.bucket = bucket;
        }

        private void touch() {
            this.lastAccess = System.currentTimeMillis();
        }
    }
}

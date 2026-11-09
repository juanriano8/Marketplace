package com.marketplace.api.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.marketplace.api.shared.cache.CacheNames;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

/**
 * Cache en memoria (Caffeine) del catalogo publico.
 *
 * <p>El catalogo se lee mucho mas de lo que se escribe y cada lectura hace una consulta paginada
 * mas una consulta de stock por producto. Se cachean la pagina y el detalle durante pocos segundos
 * para absorber rafagas de trafico sin servir datos viejos: cualquier cambio de producto o de stock
 * invalida las dos caches de golpe (ver {@code @CacheEvict} en los servicios).</p>
 *
 * <p>El TTL es deliberadamente corto. La cache es una optimizacion, no la fuente de verdad: si el
 * proceso se reinicia o Caffeine desaloja una entrada, la siguiente lectura va a PostgreSQL.</p>
 */
@Configuration
@EnableCaching(proxyTargetClass = true)
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(
        @Value("${app.cache.catalog-ttl-seconds:30}") long catalogTtlSeconds,
        @Value("${app.cache.detail-ttl-seconds:60}") long detailTtlSeconds,
        @Value("${app.cache.max-entries:1000}") long maxEntries
    ) {
        CaffeineCache catalog = new CaffeineCache(
            CacheNames.PRODUCT_CATALOG,
            Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(catalogTtlSeconds))
                .maximumSize(maxEntries)
                .recordStats()
                .build()
        );

        CaffeineCache detail = new CaffeineCache(
            CacheNames.PRODUCT_DETAIL,
            Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(detailTtlSeconds))
                .maximumSize(maxEntries)
                .recordStats()
                .build()
        );

        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(catalog, detail));
        return manager;
    }
}

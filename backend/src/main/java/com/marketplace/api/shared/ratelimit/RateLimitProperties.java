package com.marketplace.api.shared.ratelimit;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Límites del control de tasa, configurables por entorno.
 *
 * <p>Los valores por defecto son deliberadamente generosos: la suite end-to-end lanza decenas de
 * peticiones seguidas desde una misma IP y no debe chocar con el limitador. En producción se bajan
 * con variables de entorno sin tocar código.</p>
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {

    /** Interruptor general. Permite desactivar el limitador en pruebas puntuales. */
    private boolean enabled = true;

    /** Peticiones por minuto para clientes sin autenticar (catálogo público). */
    private long anonymousPerMinute = 60;

    /** Peticiones por minuto para un usuario autenticado, sea cual sea su rol. */
    private long authenticatedPerMinute = 600;

    /** Peticiones por minuto para login y registro, por IP. Frena la fuerza bruta. */
    private long authPerMinute = 30;

    /** Tope de claves vigiladas a la vez; al superarlo se purgan las inactivas. */
    private int maxTrackedKeys = 10_000;

    /** Minutos sin actividad tras los que una clave puede purgarse. */
    private long idleMinutes = 10;
}

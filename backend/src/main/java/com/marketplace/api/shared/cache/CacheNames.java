package com.marketplace.api.shared.cache;

/**
 * Nombres de las caches de aplicacion.
 *
 * <p>Se centralizan aqui para que los servicios que leen el catalogo y los que lo invalidan
 * (productos e inventario) usen exactamente la misma clave sin acoplarse a la configuracion.</p>
 */
public final class CacheNames {

    /** Listado publico paginado de productos activos. */
    public static final String PRODUCT_CATALOG = "productCatalog";

    /** Detalle publico de un producto por slug. */
    public static final String PRODUCT_DETAIL = "productDetail";

    private CacheNames() {
    }
}

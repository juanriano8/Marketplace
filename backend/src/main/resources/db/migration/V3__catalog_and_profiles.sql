--
-- V3 — reorganización del modelo de datos.
--
-- Añade las tablas que faltaban para el marketplace:
--   * seller_profiles  — perfil público y agregado de rating del vendedor
--   * marcas           — catálogo de marcas (products.brand_id)
--   * producto_imagenes — galería por producto
--   * favoritos        — productos guardados por un comprador
--   * direcciones      — libreta de direcciones del comprador
--   * cupones          — cupones de descuento
--

-- ---------------------------------------------------------------------------
-- Perfil de vendedor + agregado de rating (write-behind desde reviews)
-- ---------------------------------------------------------------------------
CREATE TABLE public.seller_profiles (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    version bigint,
    user_id uuid NOT NULL,
    store_name character varying(160),
    description character varying(1000),
    logo_url character varying(255),
    rating_sum bigint NOT NULL DEFAULT 0,
    rating_count bigint NOT NULL DEFAULT 0,
    rating_avg numeric(3,2) NOT NULL DEFAULT 0,
    CONSTRAINT seller_profiles_pkey PRIMARY KEY (id),
    CONSTRAINT seller_profiles_user_unique UNIQUE (user_id),
    CONSTRAINT seller_profiles_rating_avg_check CHECK (rating_avg >= 0 AND rating_avg <= 5)
);

CREATE INDEX idx_seller_profiles_user ON public.seller_profiles USING btree (user_id);

-- Perfil inicial para los vendedores que ya existen.
INSERT INTO public.seller_profiles
    (id, created_at, updated_at, version, user_id, store_name, rating_sum, rating_count, rating_avg)
SELECT
    gen_random_uuid(),
    now(),
    now(),
    0,
    u.id,
    split_part(u.email, '@', 1),
    COALESCE(r.rating_sum, 0),
    COALESCE(r.rating_count, 0),
    COALESCE(ROUND(r.rating_sum::numeric / NULLIF(r.rating_count, 0), 2), 0)
FROM public.users u
LEFT JOIN (
    SELECT seller_id, SUM(rating) AS rating_sum, COUNT(*) AS rating_count
    FROM public.reviews
    WHERE visible = true
    GROUP BY seller_id
) r ON r.seller_id = u.id
WHERE u.role = 'ROLE_SELLER';

-- ---------------------------------------------------------------------------
-- Marcas
-- ---------------------------------------------------------------------------
CREATE TABLE public.marcas (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    version bigint,
    name character varying(120) NOT NULL,
    slug character varying(140) NOT NULL,
    logo_url character varying(255),
    active boolean NOT NULL DEFAULT true,
    CONSTRAINT marcas_pkey PRIMARY KEY (id),
    CONSTRAINT marcas_name_unique UNIQUE (name),
    CONSTRAINT marcas_slug_unique UNIQUE (slug)
);

ALTER TABLE public.products ADD COLUMN brand_id uuid;

ALTER TABLE ONLY public.products
    ADD CONSTRAINT fk_products_brand FOREIGN KEY (brand_id) REFERENCES public.marcas(id);

CREATE INDEX idx_products_brand ON public.products USING btree (brand_id);

-- ---------------------------------------------------------------------------
-- Galería de imágenes por producto
-- ---------------------------------------------------------------------------
CREATE TABLE public.producto_imagenes (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    version bigint,
    product_id uuid NOT NULL,
    url character varying(255) NOT NULL,
    alt character varying(160),
    position integer NOT NULL DEFAULT 0,
    CONSTRAINT producto_imagenes_pkey PRIMARY KEY (id),
    CONSTRAINT fk_producto_imagenes_product FOREIGN KEY (product_id)
        REFERENCES public.products(id) ON DELETE CASCADE
);

CREATE INDEX idx_producto_imagenes_product
    ON public.producto_imagenes USING btree (product_id, position);

-- La imagen única que ya existía pasa a ser la primera de la galería.
INSERT INTO public.producto_imagenes
    (id, created_at, updated_at, version, product_id, url, alt, position)
SELECT
    gen_random_uuid(),
    now(),
    now(),
    0,
    p.id,
    p.image_url,
    p.name,
    0
FROM public.products p
WHERE p.image_url IS NOT NULL AND p.image_url <> '';

-- ---------------------------------------------------------------------------
-- Favoritos
-- ---------------------------------------------------------------------------
CREATE TABLE public.favoritos (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    version bigint,
    user_id uuid NOT NULL,
    product_id uuid NOT NULL,
    CONSTRAINT favoritos_pkey PRIMARY KEY (id),
    CONSTRAINT favoritos_user_product_unique UNIQUE (user_id, product_id),
    CONSTRAINT fk_favoritos_product FOREIGN KEY (product_id)
        REFERENCES public.products(id) ON DELETE CASCADE
);

CREATE INDEX idx_favoritos_user ON public.favoritos USING btree (user_id);

-- ---------------------------------------------------------------------------
-- Direcciones del comprador
-- ---------------------------------------------------------------------------
CREATE TABLE public.direcciones (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    version bigint,
    user_id uuid NOT NULL,
    label character varying(60),
    recipient character varying(160) NOT NULL,
    line1 character varying(255) NOT NULL,
    line2 character varying(255),
    city character varying(120) NOT NULL,
    state_region character varying(120),
    postal_code character varying(30),
    country character varying(80) NOT NULL DEFAULT 'Colombia',
    phone character varying(40),
    is_default boolean NOT NULL DEFAULT false,
    CONSTRAINT direcciones_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_direcciones_user ON public.direcciones USING btree (user_id);

-- ---------------------------------------------------------------------------
-- Cupones
-- ---------------------------------------------------------------------------
CREATE TABLE public.cupones (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    version bigint,
    code character varying(40) NOT NULL,
    type character varying(20) NOT NULL,
    value numeric(19,2) NOT NULL,
    min_amount numeric(19,2),
    max_uses integer,
    uses integer NOT NULL DEFAULT 0,
    active boolean NOT NULL DEFAULT true,
    valid_from timestamp(6) with time zone,
    valid_to timestamp(6) with time zone,
    CONSTRAINT cupones_pkey PRIMARY KEY (id),
    CONSTRAINT cupones_code_unique UNIQUE (code),
    CONSTRAINT cupones_type_check CHECK (type IN ('PERCENT', 'FIXED')),
    CONSTRAINT cupones_value_check CHECK (value > 0),
    CONSTRAINT cupones_uses_check CHECK (uses >= 0)
);

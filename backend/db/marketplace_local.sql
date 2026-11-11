--
-- PostgreSQL database dump
--

-- Dumped from database version 16.9
-- Dumped by pg_dump version 16.9

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: cart_items; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cart_items (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    version bigint,
    currency_code character varying(3) NOT NULL,
    product_id uuid NOT NULL,
    product_name character varying(255) NOT NULL,
    quantity integer NOT NULL,
    seller_id uuid NOT NULL,
    unit_price numeric(19,2) NOT NULL,
    cart_id uuid NOT NULL
);


--
-- Name: carts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.carts (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    version bigint,
    buyer_id uuid NOT NULL,
    status character varying(20) NOT NULL,
    CONSTRAINT carts_status_check CHECK (((status)::text = ANY (ARRAY[('ACTIVE'::character varying)::text, ('CHECKED_OUT'::character varying)::text, ('ABANDONED'::character varying)::text])))
);


--
-- Name: cupones; Type: TABLE; Schema: public; Owner: -
--

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
    uses integer DEFAULT 0 NOT NULL,
    active boolean DEFAULT true NOT NULL,
    valid_from timestamp(6) with time zone,
    valid_to timestamp(6) with time zone,
    CONSTRAINT cupones_type_check CHECK (((type)::text = ANY ((ARRAY['PERCENT'::character varying, 'FIXED'::character varying])::text[]))),
    CONSTRAINT cupones_uses_check CHECK ((uses >= 0)),
    CONSTRAINT cupones_value_check CHECK ((value > (0)::numeric))
);


--
-- Name: direcciones; Type: TABLE; Schema: public; Owner: -
--

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
    country character varying(80) DEFAULT 'Colombia'::character varying NOT NULL,
    phone character varying(40),
    is_default boolean DEFAULT false NOT NULL
);


--
-- Name: favoritos; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.favoritos (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    version bigint,
    user_id uuid NOT NULL,
    product_id uuid NOT NULL
);


--
-- Name: flyway_schema_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


--
-- Name: marcas; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.marcas (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    version bigint,
    name character varying(120) NOT NULL,
    slug character varying(140) NOT NULL,
    logo_url character varying(255),
    active boolean DEFAULT true NOT NULL
);


--
-- Name: notifications; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.notifications (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    version bigint,
    user_id uuid NOT NULL,
    type character varying(40) NOT NULL,
    title character varying(160) NOT NULL,
    message character varying(500) NOT NULL,
    link character varying(255),
    is_read boolean NOT NULL,
    read_at timestamp(6) with time zone
);


--
-- Name: order_items; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.order_items (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    version bigint,
    currency_code character varying(3) NOT NULL,
    product_id uuid NOT NULL,
    product_name character varying(255) NOT NULL,
    quantity integer NOT NULL,
    unit_price numeric(19,2) NOT NULL,
    sub_order_id uuid NOT NULL
);


--
-- Name: orders; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.orders (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    version bigint,
    buyer_id uuid NOT NULL,
    currency_code character varying(3) NOT NULL,
    order_number character varying(255) NOT NULL,
    paid_at timestamp(6) with time zone,
    payment_reference character varying(255),
    status character varying(30) NOT NULL,
    total_amount numeric(19,2) NOT NULL,
    CONSTRAINT orders_status_check CHECK (((status)::text = ANY (ARRAY[('PENDING_PAYMENT'::character varying)::text, ('PAID'::character varying)::text, ('PAYMENT_FAILED'::character varying)::text, ('CANCELLED'::character varying)::text, ('COMPLETED'::character varying)::text])))
);


--
-- Name: producto_imagenes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.producto_imagenes (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    version bigint,
    product_id uuid NOT NULL,
    url character varying(255) NOT NULL,
    alt character varying(160),
    "position" integer DEFAULT 0 NOT NULL
);


--
-- Name: products; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.products (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    version bigint,
    category character varying(255),
    currency_code character varying(3) NOT NULL,
    description text,
    image_url character varying(255),
    name character varying(255) NOT NULL,
    price numeric(19,2) NOT NULL,
    seller_id uuid NOT NULL,
    slug character varying(255) NOT NULL,
    status character varying(255) NOT NULL,
    stock_quantity integer NOT NULL,
    brand_id uuid,
    CONSTRAINT products_status_check CHECK (((status)::text = ANY (ARRAY[('PENDING_APPROVAL'::character varying)::text, ('ACTIVE'::character varying)::text, ('REJECTED'::character varying)::text, ('INACTIVE'::character varying)::text])))
);


--
-- Name: reviews; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.reviews (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    version bigint,
    buyer_id uuid NOT NULL,
    comment character varying(2000),
    product_id uuid NOT NULL,
    rating integer NOT NULL,
    seller_id uuid NOT NULL,
    sub_order_id uuid,
    title character varying(150),
    verified_purchase boolean NOT NULL,
    visible boolean NOT NULL
);


--
-- Name: seller_profiles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.seller_profiles (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    version bigint,
    user_id uuid NOT NULL,
    store_name character varying(160),
    description character varying(1000),
    logo_url character varying(255),
    rating_sum bigint DEFAULT 0 NOT NULL,
    rating_count bigint DEFAULT 0 NOT NULL,
    rating_avg numeric(3,2) DEFAULT 0 NOT NULL,
    CONSTRAINT seller_profiles_rating_avg_check CHECK (((rating_avg >= (0)::numeric) AND (rating_avg <= (5)::numeric)))
);


--
-- Name: stock_items; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.stock_items (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    version bigint,
    available_quantity integer NOT NULL,
    last_adjusted_at timestamp(6) with time zone,
    low_stock_threshold integer NOT NULL,
    product_id uuid NOT NULL,
    reserved_quantity integer NOT NULL,
    seller_id uuid NOT NULL
);


--
-- Name: stock_movements; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.stock_movements (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    version bigint,
    movement_type character varying(30) NOT NULL,
    performed_by uuid,
    product_id uuid NOT NULL,
    quantity_after integer NOT NULL,
    quantity_before integer NOT NULL,
    quantity_delta integer NOT NULL,
    reason character varying(500),
    reference character varying(120),
    seller_id uuid NOT NULL,
    CONSTRAINT stock_movements_movement_type_check CHECK (((movement_type)::text = ANY (ARRAY[('INITIAL'::character varying)::text, ('ADJUSTMENT'::character varying)::text, ('RESERVATION'::character varying)::text, ('SALE'::character varying)::text, ('CANCELLATION'::character varying)::text, ('ADMIN_CORRECTION'::character varying)::text])))
);


--
-- Name: sub_orders; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sub_orders (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    version bigint,
    carrier character varying(255),
    currency_code character varying(3) NOT NULL,
    delivered_at timestamp(6) with time zone,
    seller_id uuid NOT NULL,
    shipped_at timestamp(6) with time zone,
    status character varying(20) NOT NULL,
    subtotal numeric(19,2) NOT NULL,
    tracking_number character varying(255),
    order_id uuid NOT NULL,
    CONSTRAINT sub_orders_status_check CHECK (((status)::text = ANY (ARRAY[('PENDING'::character varying)::text, ('PROCESSING'::character varying)::text, ('SHIPPED'::character varying)::text, ('DELIVERED'::character varying)::text, ('CANCELLED'::character varying)::text])))
);


--
-- Name: users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.users (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    version bigint,
    email character varying(255) NOT NULL,
    enabled boolean NOT NULL,
    password character varying(255) NOT NULL,
    role character varying(255) NOT NULL,
    seller_approved boolean,
    CONSTRAINT users_role_check CHECK (((role)::text = ANY (ARRAY[('ROLE_ADMIN'::character varying)::text, ('ROLE_SELLER'::character varying)::text, ('ROLE_BUYER'::character varying)::text])))
);


--
-- Data for Name: cart_items; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.cart_items (id, created_at, updated_at, version, currency_code, product_id, product_name, quantity, seller_id, unit_price, cart_id) FROM stdin;
d9e92aa6-b793-414b-a9cd-a2b6c89852d3	2026-11-11 16:27:18.597375-05	2026-11-11 16:27:18.597375-05	0	USD	a2dc42df-bdf6-4073-af1e-3421ad37fef6	Multimetro Digital RMS Cat III 1000V	2	ffdd01be-0ee6-4f48-b1e7-f8784d1cd38e	78.50	996de87a-d569-4c8b-9d35-4c8929a494fe
866432b3-b19e-4c8c-a37c-e790b0c04e8a	2026-11-11 16:27:18.630946-05	2026-11-11 16:27:18.630946-05	0	USD	7e8a1202-f466-4b9f-a79a-c0be3ea1a773	Teclado Mecanico RGB	1	6c3da538-6bc3-45e0-b90f-8dd626d82ea7	89.50	996de87a-d569-4c8b-9d35-4c8929a494fe
\.


--
-- Data for Name: carts; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.carts (id, created_at, updated_at, version, buyer_id, status) FROM stdin;
996de87a-d569-4c8b-9d35-4c8929a494fe	2026-11-11 16:27:18.591523-05	2026-11-11 16:27:18.680577-05	1	9ad84fee-ca8c-48c7-85fd-970d87535840	CHECKED_OUT
\.


--
-- Data for Name: cupones; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.cupones (id, created_at, updated_at, version, code, type, value, min_amount, max_uses, uses, active, valid_from, valid_to) FROM stdin;
\.


--
-- Data for Name: direcciones; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.direcciones (id, created_at, updated_at, version, user_id, label, recipient, line1, line2, city, state_region, postal_code, country, phone, is_default) FROM stdin;
\.


--
-- Data for Name: favoritos; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.favoritos (id, created_at, updated_at, version, user_id, product_id) FROM stdin;
\.


--
-- Data for Name: flyway_schema_history; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) FROM stdin;
1	1	initial schema	SQL	V1__initial_schema.sql	1426117518	postgres	2026-11-11 16:25:10.085144	71	t
2	2	notifications	SQL	V2__notifications.sql	-1576356331	postgres	2026-11-11 16:25:10.181815	10	t
3	3	catalog and profiles	SQL	V3__catalog_and_profiles.sql	-948458228	postgres	2026-11-11 16:25:10.199906	47	t
\.


--
-- Data for Name: marcas; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.marcas (id, created_at, updated_at, version, name, slug, logo_url, active) FROM stdin;
\.


--
-- Data for Name: notifications; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.notifications (id, created_at, updated_at, version, user_id, type, title, message, link, is_read, read_at) FROM stdin;
22a184ed-4806-4e2c-b7ef-37c64711afa5	2026-11-11 16:25:22.200437-05	2026-11-11 16:25:22.200437-05	0	ffdd01be-0ee6-4f48-b1e7-f8784d1cd38e	SELLER_VERIFIED	Cuenta verificada	Un administrador verificó tu cuenta. Ya puedes publicar productos en el catálogo.	/vendedor	f	\N
1e4ce77a-f5be-4473-bb92-bb2518bb983e	2026-11-11 16:25:22.244484-05	2026-11-11 16:25:22.244484-05	0	93db553b-1a43-405d-97a1-42be6bb834fe	SELLER_VERIFIED	Cuenta verificada	Un administrador verificó tu cuenta. Ya puedes publicar productos en el catálogo.	/vendedor	f	\N
346aa6c2-efb0-483e-98e7-2250fadc3a15	2026-11-11 16:25:22.287096-05	2026-11-11 16:25:22.287096-05	0	6c3da538-6bc3-45e0-b90f-8dd626d82ea7	SELLER_VERIFIED	Cuenta verificada	Un administrador verificó tu cuenta. Ya puedes publicar productos en el catálogo.	/vendedor	f	\N
9b3fb2b9-f24c-4998-b059-7de05001864f	2026-11-11 16:25:22.330318-05	2026-11-11 16:25:22.330318-05	0	3496c279-3352-4953-ad36-9d7c5e0a88c7	SELLER_VERIFIED	Cuenta verificada	Un administrador verificó tu cuenta. Ya puedes publicar productos en el catálogo.	/vendedor	f	\N
738aa2c9-2291-47f2-bef0-6e47d74d4a28	2026-11-11 16:25:22.373988-05	2026-11-11 16:25:22.373988-05	0	5d33e0da-0113-4e71-8f44-8b88609209af	SELLER_VERIFIED	Cuenta verificada	Un administrador verificó tu cuenta. Ya puedes publicar productos en el catálogo.	/vendedor	f	\N
077f6de8-7828-47b4-ad99-610b93d2e788	2026-11-11 16:25:26.636394-05	2026-11-11 16:25:26.636394-05	0	ffdd01be-0ee6-4f48-b1e7-f8784d1cd38e	PRODUCT_APPROVED	Producto aprobado	Tu producto «Multimetro Digital RMS Cat III 1000V» ya está visible en el catálogo.	/vendedor	f	\N
0adf53b9-bcf9-4c9d-9fca-58b00e86caba	2026-11-11 16:25:26.648968-05	2026-11-11 16:25:26.648968-05	0	ffdd01be-0ee6-4f48-b1e7-f8784d1cd38e	PRODUCT_APPROVED	Producto aprobado	Tu producto «Camara Termica Mini Sensor» ya está visible en el catálogo.	/vendedor	f	\N
6581bfa7-aee8-4120-9c91-1191804c5890	2026-11-11 16:25:26.661471-05	2026-11-11 16:25:26.661471-05	0	ffdd01be-0ee6-4f48-b1e7-f8784d1cd38e	PRODUCT_APPROVED	Producto aprobado	Tu producto «Estacion de Soldadura Digital» ya está visible en el catálogo.	/vendedor	f	\N
6fc4ebbb-9ebe-4a80-9082-2a8d3b65abe4	2026-11-11 16:25:26.671589-05	2026-11-11 16:25:26.671589-05	0	93db553b-1a43-405d-97a1-42be6bb834fe	PRODUCT_APPROVED	Producto aprobado	Tu producto «Atornillador Brushless 18V» ya está visible en el catálogo.	/vendedor	f	\N
bcc44963-1749-4479-8058-9382e0661f52	2026-11-11 16:25:26.683449-05	2026-11-11 16:25:26.683449-05	0	93db553b-1a43-405d-97a1-42be6bb834fe	PRODUCT_APPROVED	Producto aprobado	Tu producto «Medidor Laser 60m Pro» ya está visible en el catálogo.	/vendedor	f	\N
077bdff2-d296-44ca-b725-ddd9f5977cf8	2026-11-11 16:25:26.693128-05	2026-11-11 16:25:26.693128-05	0	93db553b-1a43-405d-97a1-42be6bb834fe	PRODUCT_APPROVED	Producto aprobado	Tu producto «Engrapadora Neumatica HD» ya está visible en el catálogo.	/vendedor	f	\N
c73347e8-3739-4a39-a739-5706d5a513e8	2026-11-11 16:25:26.703521-05	2026-11-11 16:25:26.703521-05	0	6c3da538-6bc3-45e0-b90f-8dd626d82ea7	PRODUCT_APPROVED	Producto aprobado	Tu producto «Silla Ergonomica Ejecutiva» ya está visible en el catálogo.	/vendedor	f	\N
29919a65-a388-412a-9878-0087996c7f36	2026-11-11 16:25:26.714734-05	2026-11-11 16:25:26.714734-05	0	6c3da538-6bc3-45e0-b90f-8dd626d82ea7	PRODUCT_APPROVED	Producto aprobado	Tu producto «Teclado Mecanico RGB» ya está visible en el catálogo.	/vendedor	f	\N
8da7e1b1-06d6-4f54-9b8f-f6baac38984c	2026-11-11 16:25:26.723333-05	2026-11-11 16:25:26.723333-05	0	6c3da538-6bc3-45e0-b90f-8dd626d82ea7	PRODUCT_APPROVED	Producto aprobado	Tu producto «Audifonos Cancelacion de Ruido» ya está visible en el catálogo.	/vendedor	f	\N
19d60017-bfcf-4a19-8f7c-93b40b066598	2026-11-11 16:25:26.733954-05	2026-11-11 16:25:26.733954-05	0	3496c279-3352-4953-ad36-9d7c5e0a88c7	PRODUCT_APPROVED	Producto aprobado	Tu producto «Cable Cobre Puro Rollo 100m» ya está visible en el catálogo.	/vendedor	f	\N
1d0c8c8c-7efd-4acf-a2d5-fd848c84198b	2026-11-11 16:25:26.743761-05	2026-11-11 16:25:26.743761-05	0	3496c279-3352-4953-ad36-9d7c5e0a88c7	PRODUCT_APPROVED	Producto aprobado	Tu producto «Cinta Aislante Industrial x10» ya está visible en el catálogo.	/vendedor	f	\N
02a28a21-c431-40e2-ac8e-5f151f2669f7	2026-11-11 16:25:26.752855-05	2026-11-11 16:25:26.752855-05	0	3496c279-3352-4953-ad36-9d7c5e0a88c7	PRODUCT_APPROVED	Producto aprobado	Tu producto «Tuberia PVC 2 pulgadas x 3m» ya está visible en el catálogo.	/vendedor	f	\N
36c9d9f1-2ca7-4af4-860c-9b13fb1502fa	2026-11-11 16:25:26.762513-05	2026-11-11 16:25:26.762513-05	0	5d33e0da-0113-4e71-8f44-8b88609209af	PRODUCT_APPROVED	Producto aprobado	Tu producto «Laptop 14 pulgadas i5 16GB» ya está visible en el catálogo.	/vendedor	f	\N
9ed2a1e0-f376-4f84-813a-525bfe768e73	2026-11-11 16:25:26.771903-05	2026-11-11 16:25:26.771903-05	0	5d33e0da-0113-4e71-8f44-8b88609209af	PRODUCT_APPROVED	Producto aprobado	Tu producto «Monitor 27 pulgadas QHD» ya está visible en el catálogo.	/vendedor	f	\N
cd685e47-8d8c-47ee-90a2-6cfd99778c92	2026-11-11 16:25:26.782331-05	2026-11-11 16:25:26.782331-05	0	5d33e0da-0113-4e71-8f44-8b88609209af	PRODUCT_APPROVED	Producto aprobado	Tu producto «Mouse Inalambrico Ergonomico» ya está visible en el catálogo.	/vendedor	f	\N
d393cee5-32ce-4664-ad95-1c614fa6824f	2026-11-11 16:27:18.724178-05	2026-11-11 16:27:18.724178-05	0	9ad84fee-ca8c-48c7-85fd-970d87535840	ORDER_SHIPPED	Pedido despachado	Tu pedido ORD-172617230D5B4685 fue despachado. Guía: GU-2026-0001.	/mis-compras	f	\N
533ec8bd-d3eb-40bc-98cd-0548e6dbfe5e	2026-11-11 16:27:19.771638-05	2026-11-11 16:27:19.771638-05	0	ffdd01be-0ee6-4f48-b1e7-f8784d1cd38e	REVIEW_RECEIVED	Nueva reseña	Recibiste una reseña de 5★ para «Multimetro Digital RMS Cat III 1000V».	/vendedor	f	\N
\.


--
-- Data for Name: order_items; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.order_items (id, created_at, updated_at, version, currency_code, product_id, product_name, quantity, unit_price, sub_order_id) FROM stdin;
0ebc758e-a8e6-47b0-b75a-bcc12fef16c6	2026-11-11 16:27:18.648225-05	2026-11-11 16:27:18.648225-05	0	USD	a2dc42df-bdf6-4073-af1e-3421ad37fef6	Multimetro Digital RMS Cat III 1000V	2	78.50	8f3a983c-a0e7-4c7a-bdc9-a0a5a4904589
afc8e752-57bf-4e55-9ec7-0c8cc6748c20	2026-11-11 16:27:18.648225-05	2026-11-11 16:27:18.648225-05	0	USD	7e8a1202-f466-4b9f-a79a-c0be3ea1a773	Teclado Mecanico RGB	1	89.50	73f9d562-6d9f-4556-a57a-487b6e8ba14c
\.


--
-- Data for Name: orders; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.orders (id, created_at, updated_at, version, buyer_id, currency_code, order_number, paid_at, payment_reference, status, total_amount) FROM stdin;
4baf680b-cfd6-4333-9664-fb30f209e88f	2026-11-11 16:27:18.648225-05	2026-11-11 16:27:18.680577-05	1	9ad84fee-ca8c-48c7-85fd-970d87535840	USD	ORD-172617230D5B4685	2026-11-11 16:27:18.680577-05	stub-4baf680b-cfd6-4333-9664-fb30f209e88f-1b78f123	PAID	246.50
\.


--
-- Data for Name: producto_imagenes; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.producto_imagenes (id, created_at, updated_at, version, product_id, url, alt, "position") FROM stdin;
\.


--
-- Data for Name: products; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.products (id, created_at, updated_at, version, category, currency_code, description, image_url, name, price, seller_id, slug, status, stock_quantity, brand_id) FROM stdin;
a2dc42df-bdf6-4073-af1e-3421ad37fef6	2026-11-11 16:25:26.29793-05	2026-11-11 16:25:26.633873-05	1	Electronica	USD	Multimetro de gancho con medicion RMS real, categoria III 1000V. Incluye puntas, funda y pilas.	/images/productos/multimetro-digital-rms.jpg	Multimetro Digital RMS Cat III 1000V	78.50	ffdd01be-0ee6-4f48-b1e7-f8784d1cd38e	multimetro-digital-rms-cat-iii-1000v-f5830e55	ACTIVE	24	\N
655667a9-1958-4ba8-a4a9-bb78fc5e9fda	2026-11-11 16:25:26.334369-05	2026-11-11 16:25:26.646959-05	1	Electronica	USD	Camara termica de bolsillo con sensor 160x120, pantalla de 2.8 pulgadas y registro en microSD.	/images/productos/camara-termica-mini.jpg	Camara Termica Mini Sensor	210.00	ffdd01be-0ee6-4f48-b1e7-f8784d1cd38e	camara-termica-mini-sensor-dc392c31	ACTIVE	8	\N
7235528a-a0cd-44cb-b2fc-2ed0a9d0f741	2026-11-11 16:25:26.34807-05	2026-11-11 16:25:26.659967-05	1	Electronica	USD	Estacion de soldadura con control digital de temperatura, punta ceramica y soporte con esponja.	/images/productos/estacion-soldadura.png	Estacion de Soldadura Digital	95.00	ffdd01be-0ee6-4f48-b1e7-f8784d1cd38e	estacion-de-soldadura-digital-863e7918	ACTIVE	15	\N
4edd4370-ef69-486c-b56d-f255df28baaf	2026-11-11 16:25:26.363045-05	2026-11-11 16:25:26.670588-05	1	Herramientas	USD	Atornillador inalambrico con motor brushless, dos baterias de 2.0 Ah y maletin de transporte.	/images/productos/atornillador-brushless.jpg	Atornillador Brushless 18V	120.00	93db553b-1a43-405d-97a1-42be6bb834fe	atornillador-brushless-18v-bd1c1994	ACTIVE	12	\N
8e00be50-7d2c-4465-b5e9-856d0ee45b1b	2026-11-11 16:25:26.377921-05	2026-11-11 16:25:26.681937-05	1	Herramientas	USD	Medidor de distancia laser hasta 60 metros con calculo de area, volumen y pitagoras.	/images/productos/medidor-laser-60m.jpg	Medidor Laser 60m Pro	45.90	93db553b-1a43-405d-97a1-42be6bb834fe	medidor-laser-60m-pro-5cb111b7	ACTIVE	30	\N
e312b3cf-8129-4ab8-8d9f-d2e0f426efae	2026-11-11 16:25:26.391169-05	2026-11-11 16:25:26.691563-05	1	Herramientas	USD	Engrapadora neumatica de alta resistencia para madera, con regulador de presion y maletin.	/images/productos/engrapadora-neumatica.jpg	Engrapadora Neumatica HD	34.20	93db553b-1a43-405d-97a1-42be6bb834fe	engrapadora-neumatica-hd-203e8d82	ACTIVE	4	\N
9850b86b-6b99-4363-a5d9-54a1fe179673	2026-11-11 16:25:26.404771-05	2026-11-11 16:25:26.702236-05	1	Oficina	USD	Silla de malla con soporte lumbar ajustable, apoyabrazos 3D y piston de gas clase 4.	/images/productos/silla-ergonomica.jpg	Silla Ergonomica Ejecutiva	189.00	6c3da538-6bc3-45e0-b90f-8dd626d82ea7	silla-ergonomica-ejecutiva-288ea790	ACTIVE	10	\N
7e8a1202-f466-4b9f-a79a-c0be3ea1a773	2026-11-11 16:25:26.419498-05	2026-11-11 16:25:26.713611-05	1	Oficina	USD	Teclado mecanico con switches rojos, retroiluminacion RGB por tecla y reposamanos magnetico.	/images/productos/teclado-mecanico-rgb.jpg	Teclado Mecanico RGB	89.50	6c3da538-6bc3-45e0-b90f-8dd626d82ea7	teclado-mecanico-rgb-70e630b1	ACTIVE	18	\N
6d24881f-54bf-4d39-aba3-f2bbb965362a	2026-11-11 16:25:26.432501-05	2026-11-11 16:25:26.721316-05	1	Oficina	USD	Audifonos inalambricos con cancelacion activa, 30 horas de autonomia y estuche rigido.	/images/productos/audifonos-cancelacion.jpg	Audifonos Cancelacion de Ruido	139.99	6c3da538-6bc3-45e0-b90f-8dd626d82ea7	audifonos-cancelacion-de-ruido-191bb934	ACTIVE	2	\N
55ff3647-0db7-4173-8df2-1976f06c2213	2026-11-11 16:25:26.447399-05	2026-11-11 16:25:26.733117-05	1	Materiales	USD	Rollo de cable de cobre puro calibre 12 AWG, 100 metros, para instalacion residencial.	/images/productos/cable-cobre-100m.jpg	Cable Cobre Puro Rollo 100m	89.90	3496c279-3352-4953-ad36-9d7c5e0a88c7	cable-cobre-puro-rollo-100m-6e45e38d	ACTIVE	20	\N
47fac00e-2960-4382-b2cd-7c85e784b408	2026-11-11 16:25:26.460737-05	2026-11-11 16:25:26.741518-05	1	Materiales	USD	Paquete de 10 rollos de cinta aislante de PVC, resistente a 600V y a la humedad.	/images/productos/cinta-aislante.jpg	Cinta Aislante Industrial x10	12.50	3496c279-3352-4953-ad36-9d7c5e0a88c7	cinta-aislante-industrial-x10-de2d257c	ACTIVE	50	\N
fd639e64-14ea-4119-ad99-4705640e7a2f	2026-11-11 16:25:26.473468-05	2026-11-11 16:25:26.751849-05	1	Materiales	USD	Tubo de PVC sanitario de 2 pulgadas, 3 metros de largo, para desague y ventilacion.	/images/productos/tuberia-pvc.jpg	Tuberia PVC 2 pulgadas x 3m	8.90	3496c279-3352-4953-ad36-9d7c5e0a88c7	tuberia-pvc-2-pulgadas-x-3m-a289b278	ACTIVE	0	\N
99aa57a2-9157-43f7-ac59-ee0bc8d04298	2026-11-11 16:25:26.486094-05	2026-11-11 16:25:26.761512-05	1	Tecnologia	USD	Portatil de 14 pulgadas con procesador i5, 16 GB de RAM, SSD de 512 GB y pantalla IPS.	/images/productos/laptop-14.jpg	Laptop 14 pulgadas i5 16GB	749.00	5d33e0da-0113-4e71-8f44-8b88609209af	laptop-14-pulgadas-i5-16gb-9e2ea1cb	ACTIVE	6	\N
4d863a91-fa71-4224-a856-45e401dca893	2026-11-11 16:25:26.498977-05	2026-11-11 16:25:26.770889-05	1	Tecnologia	USD	Monitor de 27 pulgadas con resolucion QHD, panel IPS, 75 Hz y conexion HDMI y DisplayPort.	/images/productos/monitor-27-qhd.jpg	Monitor 27 pulgadas QHD	299.00	5d33e0da-0113-4e71-8f44-8b88609209af	monitor-27-pulgadas-qhd-d2b9758a	ACTIVE	9	\N
5423a36f-9641-4b95-8656-7c4e298efb43	2026-11-11 16:25:26.513139-05	2026-11-11 16:25:26.780825-05	1	Tecnologia	USD	Mouse inalambrico ergonomico con receptor USB, 1600 DPI ajustables y silencioso.	/images/productos/mouse-inalambrico.jpg	Mouse Inalambrico Ergonomico	35.00	5d33e0da-0113-4e71-8f44-8b88609209af	mouse-inalambrico-ergonomico-8aff5132	ACTIVE	25	\N
\.


--
-- Data for Name: reviews; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.reviews (id, created_at, updated_at, version, buyer_id, comment, product_id, rating, seller_id, sub_order_id, title, verified_purchase, visible) FROM stdin;
689f187f-72f2-430a-82e2-2aa09d42015f	2026-11-11 16:27:19.767402-05	2026-11-11 16:27:19.767402-05	0	9ad84fee-ca8c-48c7-85fd-970d87535840	Excelente multimetro, llego muy rapido y bien embalado.	a2dc42df-bdf6-4073-af1e-3421ad37fef6	5	ffdd01be-0ee6-4f48-b1e7-f8784d1cd38e	8f3a983c-a0e7-4c7a-bdc9-a0a5a4904589	\N	t	t
\.


--
-- Data for Name: seller_profiles; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.seller_profiles (id, created_at, updated_at, version, user_id, store_name, description, logo_url, rating_sum, rating_count, rating_avg) FROM stdin;
64886e8e-e5ba-4abb-87a5-ee9c88356d00	2026-11-11 16:27:20.922951-05	2026-11-11 16:27:20.922951-05	0	ffdd01be-0ee6-4f48-b1e7-f8784d1cd38e	\N	\N	\N	5	1	5.00
\.


--
-- Data for Name: stock_items; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.stock_items (id, created_at, updated_at, version, available_quantity, last_adjusted_at, low_stock_threshold, product_id, reserved_quantity, seller_id) FROM stdin;
35c5312f-844d-4ba0-8c5f-15751ba53e08	2026-11-11 16:25:26.336448-05	2026-11-11 16:25:26.336448-05	0	8	2026-11-11 16:25:26.336448-05	10	655667a9-1958-4ba8-a4a9-bb78fc5e9fda	0	ffdd01be-0ee6-4f48-b1e7-f8784d1cd38e
06826595-22d8-4ba0-8bbd-956178c81927	2026-11-11 16:25:26.35069-05	2026-11-11 16:25:26.35069-05	0	15	2026-11-11 16:25:26.350175-05	10	7235528a-a0cd-44cb-b2fc-2ed0a9d0f741	0	ffdd01be-0ee6-4f48-b1e7-f8784d1cd38e
f0a9e3c5-44e4-4e6f-b9b4-e9e64e597247	2026-11-11 16:25:26.365051-05	2026-11-11 16:25:26.365051-05	0	12	2026-11-11 16:25:26.365051-05	10	4edd4370-ef69-486c-b56d-f255df28baaf	0	93db553b-1a43-405d-97a1-42be6bb834fe
9eba5b61-d469-4a83-8339-9779c2893272	2026-11-11 16:25:26.378925-05	2026-11-11 16:25:26.378925-05	0	30	2026-11-11 16:25:26.378925-05	10	8e00be50-7d2c-4465-b5e9-856d0ee45b1b	0	93db553b-1a43-405d-97a1-42be6bb834fe
606ced22-1922-4ac6-9cb4-b4eb1de97a74	2026-11-11 16:25:26.393178-05	2026-11-11 16:25:26.393178-05	0	4	2026-11-11 16:25:26.393178-05	10	e312b3cf-8129-4ab8-8d9f-d2e0f426efae	0	93db553b-1a43-405d-97a1-42be6bb834fe
a8aed8fb-2146-4663-951f-96abe0bff5ee	2026-11-11 16:25:26.406283-05	2026-11-11 16:25:26.406283-05	0	10	2026-11-11 16:25:26.406283-05	10	9850b86b-6b99-4363-a5d9-54a1fe179673	0	6c3da538-6bc3-45e0-b90f-8dd626d82ea7
fe3721e8-a719-43cc-83b2-775e8edfac73	2026-11-11 16:25:26.43451-05	2026-11-11 16:25:26.43451-05	0	2	2026-11-11 16:25:26.43451-05	10	6d24881f-54bf-4d39-aba3-f2bbb965362a	0	6c3da538-6bc3-45e0-b90f-8dd626d82ea7
ac580652-4138-468a-b161-8eb94d5760e3	2026-11-11 16:25:26.448411-05	2026-11-11 16:25:26.448411-05	0	20	2026-11-11 16:25:26.448411-05	10	55ff3647-0db7-4173-8df2-1976f06c2213	0	3496c279-3352-4953-ad36-9d7c5e0a88c7
05397a6d-f503-4a52-bb28-9b930e1e3bab	2026-11-11 16:25:26.462301-05	2026-11-11 16:25:26.462301-05	0	50	2026-11-11 16:25:26.462301-05	10	47fac00e-2960-4382-b2cd-7c85e784b408	0	3496c279-3352-4953-ad36-9d7c5e0a88c7
031dda9a-0e94-4799-a222-a6a97c94eefa	2026-11-11 16:25:26.474473-05	2026-11-11 16:25:26.474473-05	0	0	2026-11-11 16:25:26.474473-05	10	fd639e64-14ea-4119-ad99-4705640e7a2f	0	3496c279-3352-4953-ad36-9d7c5e0a88c7
622b26c5-9953-4852-a499-a6c786dad751	2026-11-11 16:25:26.487603-05	2026-11-11 16:25:26.487603-05	0	6	2026-11-11 16:25:26.487603-05	10	99aa57a2-9157-43f7-ac59-ee0bc8d04298	0	5d33e0da-0113-4e71-8f44-8b88609209af
1f7341ca-7663-4d5a-8ff7-493475a5e920	2026-11-11 16:25:26.499981-05	2026-11-11 16:25:26.499981-05	0	9	2026-11-11 16:25:26.499981-05	10	4d863a91-fa71-4224-a856-45e401dca893	0	5d33e0da-0113-4e71-8f44-8b88609209af
98e9afd1-56c5-498f-8178-6ba846fb3cfe	2026-11-11 16:25:26.514142-05	2026-11-11 16:25:26.514142-05	0	25	2026-11-11 16:25:26.514142-05	10	5423a36f-9641-4b95-8656-7c4e298efb43	0	5d33e0da-0113-4e71-8f44-8b88609209af
ade58741-8310-4dc7-89a7-da3b6e37d048	2026-11-11 16:25:26.302308-05	2026-11-11 16:27:18.679367-05	2	22	2026-11-11 16:27:18.678367-05	10	a2dc42df-bdf6-4073-af1e-3421ad37fef6	0	ffdd01be-0ee6-4f48-b1e7-f8784d1cd38e
dd6bd189-4a20-45c6-b39e-4d67d596755b	2026-11-11 16:25:26.420501-05	2026-11-11 16:27:18.681582-05	2	17	2026-11-11 16:27:18.679367-05	10	7e8a1202-f466-4b9f-a79a-c0be3ea1a773	0	6c3da538-6bc3-45e0-b90f-8dd626d82ea7
\.


--
-- Data for Name: stock_movements; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.stock_movements (id, created_at, updated_at, version, movement_type, performed_by, product_id, quantity_after, quantity_before, quantity_delta, reason, reference, seller_id) FROM stdin;
4c5440d8-68c8-478c-b799-84682dd23e08	2026-11-11 16:25:26.303811-05	2026-11-11 16:25:26.303811-05	0	INITIAL	ffdd01be-0ee6-4f48-b1e7-f8784d1cd38e	a2dc42df-bdf6-4073-af1e-3421ad37fef6	24	0	24	Initial stock declared when the product was created	\N	ffdd01be-0ee6-4f48-b1e7-f8784d1cd38e
ddd98605-3fcb-4562-ac28-613d2c447669	2026-11-11 16:25:26.336448-05	2026-11-11 16:25:26.336448-05	0	INITIAL	ffdd01be-0ee6-4f48-b1e7-f8784d1cd38e	655667a9-1958-4ba8-a4a9-bb78fc5e9fda	8	0	8	Initial stock declared when the product was created	\N	ffdd01be-0ee6-4f48-b1e7-f8784d1cd38e
1d6aec81-a719-44a0-910b-441edbedd245	2026-11-11 16:25:26.35069-05	2026-11-11 16:25:26.35069-05	0	INITIAL	ffdd01be-0ee6-4f48-b1e7-f8784d1cd38e	7235528a-a0cd-44cb-b2fc-2ed0a9d0f741	15	0	15	Initial stock declared when the product was created	\N	ffdd01be-0ee6-4f48-b1e7-f8784d1cd38e
6f4ded14-d5e2-497a-b757-96a91b3488a8	2026-11-11 16:25:26.365051-05	2026-11-11 16:25:26.365051-05	0	INITIAL	93db553b-1a43-405d-97a1-42be6bb834fe	4edd4370-ef69-486c-b56d-f255df28baaf	12	0	12	Initial stock declared when the product was created	\N	93db553b-1a43-405d-97a1-42be6bb834fe
0534d9eb-395d-48b0-9cb7-a916fe4ca5ed	2026-11-11 16:25:26.378925-05	2026-11-11 16:25:26.378925-05	0	INITIAL	93db553b-1a43-405d-97a1-42be6bb834fe	8e00be50-7d2c-4465-b5e9-856d0ee45b1b	30	0	30	Initial stock declared when the product was created	\N	93db553b-1a43-405d-97a1-42be6bb834fe
48832f44-7569-4692-92d9-3436553ed004	2026-11-11 16:25:26.393178-05	2026-11-11 16:25:26.393178-05	0	INITIAL	93db553b-1a43-405d-97a1-42be6bb834fe	e312b3cf-8129-4ab8-8d9f-d2e0f426efae	4	0	4	Initial stock declared when the product was created	\N	93db553b-1a43-405d-97a1-42be6bb834fe
79167bcb-2127-4c10-81be-36ef9e5b7b9d	2026-11-11 16:25:26.406283-05	2026-11-11 16:25:26.406283-05	0	INITIAL	6c3da538-6bc3-45e0-b90f-8dd626d82ea7	9850b86b-6b99-4363-a5d9-54a1fe179673	10	0	10	Initial stock declared when the product was created	\N	6c3da538-6bc3-45e0-b90f-8dd626d82ea7
acd7ee3e-f2f2-457e-ad65-f5012a6969fa	2026-11-11 16:25:26.420501-05	2026-11-11 16:25:26.420501-05	0	INITIAL	6c3da538-6bc3-45e0-b90f-8dd626d82ea7	7e8a1202-f466-4b9f-a79a-c0be3ea1a773	18	0	18	Initial stock declared when the product was created	\N	6c3da538-6bc3-45e0-b90f-8dd626d82ea7
31c76361-7ffa-450c-aae8-f9ae6ce521bf	2026-11-11 16:25:26.43451-05	2026-11-11 16:25:26.43451-05	0	INITIAL	6c3da538-6bc3-45e0-b90f-8dd626d82ea7	6d24881f-54bf-4d39-aba3-f2bbb965362a	2	0	2	Initial stock declared when the product was created	\N	6c3da538-6bc3-45e0-b90f-8dd626d82ea7
84097098-2912-4b64-8fb8-1d6bed42d43d	2026-11-11 16:25:26.448411-05	2026-11-11 16:25:26.448411-05	0	INITIAL	3496c279-3352-4953-ad36-9d7c5e0a88c7	55ff3647-0db7-4173-8df2-1976f06c2213	20	0	20	Initial stock declared when the product was created	\N	3496c279-3352-4953-ad36-9d7c5e0a88c7
fd3ec4d7-287e-4bf0-bcf0-39f1602e45ec	2026-11-11 16:25:26.462301-05	2026-11-11 16:25:26.462301-05	0	INITIAL	3496c279-3352-4953-ad36-9d7c5e0a88c7	47fac00e-2960-4382-b2cd-7c85e784b408	50	0	50	Initial stock declared when the product was created	\N	3496c279-3352-4953-ad36-9d7c5e0a88c7
f028268a-550d-400e-a341-9b04a2e8236a	2026-11-11 16:25:26.474473-05	2026-11-11 16:25:26.474473-05	0	INITIAL	3496c279-3352-4953-ad36-9d7c5e0a88c7	fd639e64-14ea-4119-ad99-4705640e7a2f	0	0	0	Initial stock declared when the product was created	\N	3496c279-3352-4953-ad36-9d7c5e0a88c7
4834b082-6066-46de-a295-744c2167f421	2026-11-11 16:25:26.487603-05	2026-11-11 16:25:26.487603-05	0	INITIAL	5d33e0da-0113-4e71-8f44-8b88609209af	99aa57a2-9157-43f7-ac59-ee0bc8d04298	6	0	6	Initial stock declared when the product was created	\N	5d33e0da-0113-4e71-8f44-8b88609209af
895e1460-eea7-4b1b-8cb0-229dd314c2e4	2026-11-11 16:25:26.499981-05	2026-11-11 16:25:26.499981-05	0	INITIAL	5d33e0da-0113-4e71-8f44-8b88609209af	4d863a91-fa71-4224-a856-45e401dca893	9	0	9	Initial stock declared when the product was created	\N	5d33e0da-0113-4e71-8f44-8b88609209af
945ff773-08e1-4364-853e-b226303ee080	2026-11-11 16:25:26.514142-05	2026-11-11 16:25:26.514142-05	0	INITIAL	5d33e0da-0113-4e71-8f44-8b88609209af	5423a36f-9641-4b95-8656-7c4e298efb43	25	0	25	Initial stock declared when the product was created	\N	5d33e0da-0113-4e71-8f44-8b88609209af
1413a41d-88d8-4063-b1ef-21dd3df9c8ce	2026-11-11 16:27:18.661309-05	2026-11-11 16:27:18.661309-05	0	RESERVATION	ffdd01be-0ee6-4f48-b1e7-f8784d1cd38e	a2dc42df-bdf6-4073-af1e-3421ad37fef6	24	24	0	Stock held for checkout ORD-172617230D5B4685	ORD-172617230D5B4685	ffdd01be-0ee6-4f48-b1e7-f8784d1cd38e
c70be7e4-dbc8-461b-b451-94927d11f347	2026-11-11 16:27:18.672581-05	2026-11-11 16:27:18.672581-05	0	RESERVATION	6c3da538-6bc3-45e0-b90f-8dd626d82ea7	7e8a1202-f466-4b9f-a79a-c0be3ea1a773	18	18	0	Stock held for checkout ORD-172617230D5B4685	ORD-172617230D5B4685	6c3da538-6bc3-45e0-b90f-8dd626d82ea7
aa4b35b1-4678-4a7b-a011-1e63d44739db	2026-11-11 16:27:18.678367-05	2026-11-11 16:27:18.678367-05	0	SALE	ffdd01be-0ee6-4f48-b1e7-f8784d1cd38e	a2dc42df-bdf6-4073-af1e-3421ad37fef6	22	24	-2	Reservation consumed by ORD-172617230D5B4685	ORD-172617230D5B4685	ffdd01be-0ee6-4f48-b1e7-f8784d1cd38e
176722c0-0118-441f-a0d2-fdee09bc53ec	2026-11-11 16:27:18.679367-05	2026-11-11 16:27:18.679367-05	0	SALE	6c3da538-6bc3-45e0-b90f-8dd626d82ea7	7e8a1202-f466-4b9f-a79a-c0be3ea1a773	17	18	-1	Reservation consumed by ORD-172617230D5B4685	ORD-172617230D5B4685	6c3da538-6bc3-45e0-b90f-8dd626d82ea7
\.


--
-- Data for Name: sub_orders; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.sub_orders (id, created_at, updated_at, version, carrier, currency_code, delivered_at, seller_id, shipped_at, status, subtotal, tracking_number, order_id) FROM stdin;
73f9d562-6d9f-4556-a57a-487b6e8ba14c	2026-11-11 16:27:18.648225-05	2026-11-11 16:27:18.680577-05	1	\N	USD	\N	6c3da538-6bc3-45e0-b90f-8dd626d82ea7	\N	PROCESSING	89.50	\N	4baf680b-cfd6-4333-9664-fb30f209e88f
8f3a983c-a0e7-4c7a-bdc9-a0a5a4904589	2026-11-11 16:27:18.648225-05	2026-11-11 16:27:18.722611-05	2	Coordinadora	USD	\N	ffdd01be-0ee6-4f48-b1e7-f8784d1cd38e	2026-11-11 16:27:18.714029-05	SHIPPED	157.00	GU-2026-0001	4baf680b-cfd6-4333-9664-fb30f209e88f
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.users (id, created_at, updated_at, version, email, enabled, password, role, seller_approved) FROM stdin;
41417720-7dcf-4487-b8ef-6b90b8d5c638	2026-11-11 16:25:15.902461-05	2026-11-11 16:25:15.902461-05	0	admin@marketplace.com	t	$2a$10$1/UbJB7XvMhjAC0zS5WcGeiWVGgSrg0s8soZ8iDGCGNDZJdRXTg7e	ROLE_ADMIN	\N
9ad84fee-ca8c-48c7-85fd-970d87535840	2026-11-11 16:25:21.271744-05	2026-11-11 16:25:21.271744-05	0	tomas.rodriguez1@marketplace.com	t	$2a$10$P7fOW.dqcv3Wk9IKV1/n1.L5XxhG.28iJPBtYnl356XTj6NNcGVq2	ROLE_BUYER	\N
42eb4dcf-a951-46d4-bead-4369ff95343b	2026-11-11 16:25:21.36242-05	2026-11-11 16:25:21.36242-05	0	veronica.medina2@marketplace.com	t	$2a$10$Ve6ea.KhzoGwxk1nlwrpuO9mejFM/bDHhByBEhzXTDiOmcJrRsEXu	ROLE_BUYER	\N
8807648c-6a6a-460a-acf8-5d5372c899e3	2026-11-11 16:25:21.454552-05	2026-11-11 16:25:21.454552-05	0	daniela.rodriguez6@marketplace.com	t	$2a$10$.T6wwxzuZDQQsUtfsl9qt.Q0cN1RAU5OAIyHvxW25SjnLcH.Zu7nG	ROLE_BUYER	\N
19ecca92-9636-46b1-9b50-4f41c36c5220	2026-11-11 16:25:21.543455-05	2026-11-11 16:25:21.543455-05	0	isabella.jimenez8@marketplace.com	t	$2a$10$Mxhcps0pbwvfHj5g4zkSwetPHXff2TQvhyvrqbqMzcUKPsHMkQb5K	ROLE_BUYER	\N
49ae931f-7ef4-4e57-90ec-af997c4db3ad	2026-11-11 16:25:21.634472-05	2026-11-11 16:25:21.634472-05	0	miguel.torres10@marketplace.com	t	$2a$10$R5rgD3OJFa/0SnpdLN4wBOkK1TTG2hzrVKLuXZOM5iEDO6xEluc3e	ROLE_BUYER	\N
f687ca33-97e7-4bb8-8174-d7acda4bc1de	2026-11-11 16:25:21.723005-05	2026-11-11 16:25:21.723005-05	0	natalia.guzman11@marketplace.com	t	$2a$10$/0Anuxf0jgaPSQTlibCaZ.iAw539vFTFwJNGJSvN//rPmvFD9.UNW	ROLE_BUYER	\N
3995605b-cfc0-44f6-8998-d5aea9daea3e	2026-11-11 16:25:21.81229-05	2026-11-11 16:25:21.81229-05	0	juliana.ortiz12@marketplace.com	t	$2a$10$qtjsdOPXw1TdY7mc5srwM.jEaff4om1retXkMekoq9IwsXG4TBrly	ROLE_BUYER	\N
c5183d27-369c-4e56-a892-41dca8482307	2026-11-11 16:25:21.908148-05	2026-11-11 16:25:21.908148-05	0	diana.perez13@marketplace.com	t	$2a$10$Z2meqQ2Yn6gferOWRZkhAu1lYxIEMHJXD6NDOBphF/4ZIY1qQ51be	ROLE_BUYER	\N
7a0436c0-2789-4b5f-a79c-ed586da83c21	2026-11-11 16:25:22.003759-05	2026-11-11 16:25:22.003759-05	0	julian.munoz14@marketplace.com	t	$2a$10$zZc4gFqlznc03YOjEwrSl.0Ti1NhRmJL5KqbbG25SG2trF.8gBwzS	ROLE_BUYER	\N
d69aab83-29da-49fd-88af-8e7b248c2c83	2026-11-11 16:25:22.092252-05	2026-11-11 16:25:22.092252-05	0	alejandra.rojas15@marketplace.com	t	$2a$10$evP7udk7KsOMoZh5iLnvR.TBDTbWPM97QxR4m4EiHDs5nIiq56Ibi	ROLE_BUYER	\N
ffdd01be-0ee6-4f48-b1e7-f8784d1cd38e	2026-11-11 16:25:20.804509-05	2026-11-11 16:25:22.191785-05	1	diego.herrera3@marketplace.com	t	$2a$10$FIaYXb2CjHrHlJ4p4zhiZOH7jVy5q9uCbJyxHLiCpzq27i/vMO4S.	ROLE_SELLER	t
93db553b-1a43-405d-97a1-42be6bb834fe	2026-11-11 16:25:20.899381-05	2026-11-11 16:25:22.24316-05	1	jorge.ramirez4@marketplace.com	t	$2a$10$y65oNMwj5LnV1mXxvSjhVeRrqS6VhuhJZtHEM9RT7g80heDha3RL.	ROLE_SELLER	t
6c3da538-6bc3-45e0-b90f-8dd626d82ea7	2026-11-11 16:25:20.990118-05	2026-11-11 16:25:22.284584-05	1	santiago.flores9@marketplace.com	t	$2a$10$2aaoUuFr/RT4lvs2PZDAaeO7ib4gHgtJiftIu7hLTa2MfkvUU88Tm	ROLE_SELLER	t
3496c279-3352-4953-ad36-9d7c5e0a88c7	2026-11-11 16:25:21.079785-05	2026-11-11 16:25:22.328296-05	1	daniel.herrera17@marketplace.com	t	$2a$10$sBD4LLH3GGmKJq7tc67hceAthw/I4NtXR95pd8YcXjTi0ibYW8Yle	ROLE_SELLER	t
5d33e0da-0113-4e71-8f44-8b88609209af	2026-11-11 16:25:21.172158-05	2026-11-11 16:25:22.372711-05	1	andres.medina21@marketplace.com	t	$2a$10$IqNJUvtgxomCvNWZgGhGFufk5wjiDgyEs4kPCFzqr9SodT3iceAtG	ROLE_SELLER	t
\.


--
-- Name: cart_items cart_items_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cart_items
    ADD CONSTRAINT cart_items_pkey PRIMARY KEY (id);


--
-- Name: carts carts_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.carts
    ADD CONSTRAINT carts_pkey PRIMARY KEY (id);


--
-- Name: cupones cupones_code_unique; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cupones
    ADD CONSTRAINT cupones_code_unique UNIQUE (code);


--
-- Name: cupones cupones_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cupones
    ADD CONSTRAINT cupones_pkey PRIMARY KEY (id);


--
-- Name: direcciones direcciones_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.direcciones
    ADD CONSTRAINT direcciones_pkey PRIMARY KEY (id);


--
-- Name: favoritos favoritos_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.favoritos
    ADD CONSTRAINT favoritos_pkey PRIMARY KEY (id);


--
-- Name: favoritos favoritos_user_product_unique; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.favoritos
    ADD CONSTRAINT favoritos_user_product_unique UNIQUE (user_id, product_id);


--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: marcas marcas_name_unique; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.marcas
    ADD CONSTRAINT marcas_name_unique UNIQUE (name);


--
-- Name: marcas marcas_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.marcas
    ADD CONSTRAINT marcas_pkey PRIMARY KEY (id);


--
-- Name: marcas marcas_slug_unique; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.marcas
    ADD CONSTRAINT marcas_slug_unique UNIQUE (slug);


--
-- Name: notifications notifications_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT notifications_pkey PRIMARY KEY (id);


--
-- Name: order_items order_items_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.order_items
    ADD CONSTRAINT order_items_pkey PRIMARY KEY (id);


--
-- Name: orders orders_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_pkey PRIMARY KEY (id);


--
-- Name: producto_imagenes producto_imagenes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.producto_imagenes
    ADD CONSTRAINT producto_imagenes_pkey PRIMARY KEY (id);


--
-- Name: products products_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT products_pkey PRIMARY KEY (id);


--
-- Name: reviews reviews_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reviews
    ADD CONSTRAINT reviews_pkey PRIMARY KEY (id);


--
-- Name: seller_profiles seller_profiles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.seller_profiles
    ADD CONSTRAINT seller_profiles_pkey PRIMARY KEY (id);


--
-- Name: seller_profiles seller_profiles_user_unique; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.seller_profiles
    ADD CONSTRAINT seller_profiles_user_unique UNIQUE (user_id);


--
-- Name: stock_items stock_items_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stock_items
    ADD CONSTRAINT stock_items_pkey PRIMARY KEY (id);


--
-- Name: stock_movements stock_movements_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stock_movements
    ADD CONSTRAINT stock_movements_pkey PRIMARY KEY (id);


--
-- Name: sub_orders sub_orders_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sub_orders
    ADD CONSTRAINT sub_orders_pkey PRIMARY KEY (id);


--
-- Name: users uk6dotkott2kjsp8vw4d0m25fb7; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email);


--
-- Name: reviews uk_review_buyer_product; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reviews
    ADD CONSTRAINT uk_review_buyer_product UNIQUE (buyer_id, product_id);


--
-- Name: stock_items uk_stock_item_product; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stock_items
    ADD CONSTRAINT uk_stock_item_product UNIQUE (product_id);


--
-- Name: orders uknthkiu7pgmnqnu86i2jyoe2v7; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT uknthkiu7pgmnqnu86i2jyoe2v7 UNIQUE (order_number);


--
-- Name: products ukostq1ec3toafnjok09y9l7dox; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT ukostq1ec3toafnjok09y9l7dox UNIQUE (slug);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- Name: idx_direcciones_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_direcciones_user ON public.direcciones USING btree (user_id);


--
-- Name: idx_favoritos_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_favoritos_user ON public.favoritos USING btree (user_id);


--
-- Name: idx_notifications_user_created; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_notifications_user_created ON public.notifications USING btree (user_id, created_at DESC);


--
-- Name: idx_notifications_user_unread; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_notifications_user_unread ON public.notifications USING btree (user_id, is_read);


--
-- Name: idx_producto_imagenes_product; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_producto_imagenes_product ON public.producto_imagenes USING btree (product_id, "position");


--
-- Name: idx_products_brand; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_products_brand ON public.products USING btree (brand_id);


--
-- Name: idx_review_product; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_review_product ON public.reviews USING btree (product_id);


--
-- Name: idx_seller_profiles_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_seller_profiles_user ON public.seller_profiles USING btree (user_id);


--
-- Name: idx_stock_movement_product; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_stock_movement_product ON public.stock_movements USING btree (product_id);


--
-- Name: idx_stock_movement_seller; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_stock_movement_seller ON public.stock_movements USING btree (seller_id);


--
-- Name: order_items fk5dv8t9ns7l4he5eqdos8idf4x; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.order_items
    ADD CONSTRAINT fk5dv8t9ns7l4he5eqdos8idf4x FOREIGN KEY (sub_order_id) REFERENCES public.sub_orders(id);


--
-- Name: favoritos fk_favoritos_product; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.favoritos
    ADD CONSTRAINT fk_favoritos_product FOREIGN KEY (product_id) REFERENCES public.products(id) ON DELETE CASCADE;


--
-- Name: producto_imagenes fk_producto_imagenes_product; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.producto_imagenes
    ADD CONSTRAINT fk_producto_imagenes_product FOREIGN KEY (product_id) REFERENCES public.products(id) ON DELETE CASCADE;


--
-- Name: products fk_products_brand; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT fk_products_brand FOREIGN KEY (brand_id) REFERENCES public.marcas(id);


--
-- Name: sub_orders fkcwqdpqgm2o3ffe8ioqvb69vgd; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sub_orders
    ADD CONSTRAINT fkcwqdpqgm2o3ffe8ioqvb69vgd FOREIGN KEY (order_id) REFERENCES public.orders(id);


--
-- Name: cart_items fkpcttvuq4mxppo8sxggjtn5i2c; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cart_items
    ADD CONSTRAINT fkpcttvuq4mxppo8sxggjtn5i2c FOREIGN KEY (cart_id) REFERENCES public.carts(id);


--
-- PostgreSQL database dump complete
--


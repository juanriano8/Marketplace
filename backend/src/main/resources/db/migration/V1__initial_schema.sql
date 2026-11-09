--
-- PostgreSQL database dump
--


-- Dumped from database version 18.6
-- Dumped by pg_dump version 18.6

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
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
    CONSTRAINT carts_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'CHECKED_OUT'::character varying, 'ABANDONED'::character varying])::text[])))
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
    CONSTRAINT orders_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING_PAYMENT'::character varying, 'PAID'::character varying, 'PAYMENT_FAILED'::character varying, 'CANCELLED'::character varying, 'COMPLETED'::character varying])::text[])))
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
    CONSTRAINT products_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING_APPROVAL'::character varying, 'ACTIVE'::character varying, 'REJECTED'::character varying, 'INACTIVE'::character varying])::text[])))
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
    CONSTRAINT stock_movements_movement_type_check CHECK (((movement_type)::text = ANY ((ARRAY['INITIAL'::character varying, 'ADJUSTMENT'::character varying, 'RESERVATION'::character varying, 'SALE'::character varying, 'CANCELLATION'::character varying, 'ADMIN_CORRECTION'::character varying])::text[])))
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
    CONSTRAINT sub_orders_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'PROCESSING'::character varying, 'SHIPPED'::character varying, 'DELIVERED'::character varying, 'CANCELLED'::character varying])::text[])))
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
    CONSTRAINT users_role_check CHECK (((role)::text = ANY ((ARRAY['ROLE_ADMIN'::character varying, 'ROLE_SELLER'::character varying, 'ROLE_BUYER'::character varying])::text[])))
);


--
-- Data for Name: cart_items; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: carts; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: order_items; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: orders; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: products; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: reviews; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: stock_items; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: stock_movements; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: sub_orders; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: -
--



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
-- Name: idx_review_product; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_review_product ON public.reviews USING btree (product_id);


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
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: -
--

GRANT ALL ON SCHEMA public TO cloudsqlsuperuser;


--
-- PostgreSQL database dump complete
--

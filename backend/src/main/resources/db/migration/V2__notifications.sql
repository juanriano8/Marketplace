--
-- Notificaciones internas del marketplace.
--
-- Cada fila pertenece a un usuario (comprador, vendedor o administrador) y se
-- crea desde un evento de dominio: vendedor verificado, producto moderado,
-- pedido despachado/entregado o reseña recibida.
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

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT notifications_pkey PRIMARY KEY (id);

CREATE INDEX idx_notifications_user_created
    ON public.notifications USING btree (user_id, created_at DESC);

CREATE INDEX idx_notifications_user_unread
    ON public.notifications USING btree (user_id, is_read);

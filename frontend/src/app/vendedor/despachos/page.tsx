'use client';

import { useCallback, useEffect, useState } from 'react';
import { apiFetch, errorMessage, type Page } from '@/lib/api';
import { useAuth } from '@/lib/auth';
import type { SubOrder, SubOrderStatus } from '@/lib/types';
import { RequireAuth } from '@/components/RequireAuth';
import { EmptyState, ErrorBox, Loading, PageHeader, Success } from '@/components/feedback';
import { StatusBadge, formatDate, formatMoney } from '@/components/ui';

const FILTERS: { value: SubOrderStatus | 'ALL'; label: string }[] = [
  { value: 'PROCESSING', label: 'Por despachar' },
  { value: 'SHIPPED', label: 'Despachadas' },
  { value: 'PENDING', label: 'Pendientes de pago' },
  { value: 'DELIVERED', label: 'Entregadas' },
  { value: 'CANCELLED', label: 'Canceladas' },
  { value: 'ALL', label: 'Todas' },
];

export default function SellerShipmentsPage() {
  return (
    <RequireAuth role="ROLE_SELLER">
      <Shipments />
    </RequireAuth>
  );
}

function Shipments() {
  const { session } = useAuth();
  const [filter, setFilter] = useState<SubOrderStatus | 'ALL'>('PROCESSING');
  const [subOrders, setSubOrders] = useState<SubOrder[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [feedback, setFeedback] = useState<{ tone: 'ok' | 'error'; text: string } | null>(null);

  const [shipping, setShipping] = useState<SubOrder | null>(null);
  const [tracking, setTracking] = useState('');
  const [carrier, setCarrier] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);

  const load = useCallback(async () => {
    if (!session?.token) return;
    setLoading(true);
    setError(null);
    try {
      const query = filter === 'ALL' ? '' : `status=${filter}&`;
      const page = await apiFetch<Page<SubOrder>>(
        `/api/v1/seller/orders?${query}page=0&size=100&sort=createdAt,desc`,
        { token: session.token },
      );
      setSubOrders(page.content);
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [session?.token, filter]);

  useEffect(() => {
    void load();
  }, [load]);

  async function submitShipment(event: React.FormEvent) {
    event.preventDefault();
    if (!session?.token || !shipping) return;
    setSubmitting(true);
    setFormError(null);
    try {
      await apiFetch<SubOrder>(`/api/v1/seller/orders/${shipping.subOrderId}/ship`, {
        method: 'PATCH',
        token: session.token,
        body: { trackingNumber: tracking.trim(), carrier: carrier.trim() || undefined },
      });
      setShipping(null);
      setTracking('');
      setCarrier('');
      setFeedback({
        tone: 'ok',
        text: 'Despacho registrado. El comprador ya puede dejar su reseña.',
      });
      await load();
    } catch (err) {
      setFormError(errorMessage(err));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div>
      <PageHeader
        title="Despachos"
        description="Sub-órdenes asignadas a tu tienda: registra el número de guía al enviarlas."
        action={
          <button type="button" className="btn btn-secondary btn-sm" onClick={() => void load()}>
            Actualizar
          </button>
        }
      />

      <div className="mb-4 flex flex-wrap gap-2">
        {FILTERS.map((option) => (
          <button
            key={option.value}
            type="button"
            className={`btn btn-sm ${filter === option.value ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => setFilter(option.value)}
          >
            {option.label}
          </button>
        ))}
      </div>

      {feedback && (
        <div className="mb-4">
          {feedback.tone === 'ok' ? (
            <Success>{feedback.text}</Success>
          ) : (
            <ErrorBox>{feedback.text}</ErrorBox>
          )}
        </div>
      )}

      {error && (
        <div className="mb-4">
          <ErrorBox>{error}</ErrorBox>
        </div>
      )}

      {shipping && (
        <form onSubmit={submitShipment} className="card mb-6 p-5">
          <h2 className="font-bold text-ink-900">Registrar despacho</h2>
          <p className="mt-1 text-xs text-ink-500">
            Sub-orden {shipping.subOrderId.slice(0, 8)}… ·{' '}
            {formatMoney(shipping.subtotal, shipping.currencyCode)} ·{' '}
            {shipping.items.reduce((total, item) => total + item.quantity, 0)} unidad(es)
          </p>

          <div className="mt-4 grid gap-3 sm:grid-cols-2">
            <div>
              <label className="label" htmlFor="tracking">
                Número de guía *
              </label>
              <input
                id="tracking"
                className="input"
                maxLength={120}
                value={tracking}
                onChange={(event) => setTracking(event.target.value)}
                required
                placeholder="TRK-9938472635"
              />
            </div>
            <div>
              <label className="label" htmlFor="carrier">
                Transportadora
              </label>
              <input
                id="carrier"
                className="input"
                maxLength={120}
                value={carrier}
                onChange={(event) => setCarrier(event.target.value)}
                placeholder="DHL, Servientrega, FedEx…"
              />
            </div>
          </div>

          {formError && (
            <div className="mt-3">
              <ErrorBox>{formError}</ErrorBox>
            </div>
          )}

          <div className="mt-4 flex gap-2">
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? 'Registrando…' : 'Marcar como despachada'}
            </button>
            <button type="button" className="btn btn-secondary" onClick={() => setShipping(null)}>
              Cancelar
            </button>
          </div>
        </form>
      )}

      {loading ? (
        <Loading />
      ) : subOrders.length === 0 ? (
        <EmptyState
          title="No hay sub-órdenes en este estado"
          description="Cuando un comprador pague un producto tuyo, aparecerá aquí para despacharlo."
        />
      ) : (
        <div className="space-y-3">
          {subOrders.map((subOrder) => (
            <article key={subOrder.subOrderId} className="card p-4">
              <div className="flex flex-wrap items-center justify-between gap-2">
                <div>
                  <p className="font-mono text-xs text-ink-400">
                    {subOrder.subOrderId.slice(0, 8)}…
                  </p>
                  <p className="text-sm font-semibold text-ink-900">
                    {subOrder.items.length} producto(s) ·{' '}
                    {formatMoney(subOrder.subtotal, subOrder.currencyCode)}
                  </p>
                </div>
                <div className="flex items-center gap-2">
                  <StatusBadge status={subOrder.status} />
                  {subOrder.status === 'PROCESSING' && (
                    <button
                      type="button"
                      className="btn btn-primary btn-sm"
                      onClick={() => {
                        setShipping(subOrder);
                        setTracking('');
                        setCarrier('');
                        setFormError(null);
                      }}
                    >
                      Registrar despacho
                    </button>
                  )}
                </div>
              </div>

              <ul className="mt-3 space-y-1 border-t border-ink-100 pt-3 text-sm text-ink-600">
                {subOrder.items.map((item) => (
                  <li key={item.orderItemId} className="flex justify-between">
                    <span>
                      {item.productName} × {item.quantity}
                    </span>
                    <span>{formatMoney(item.lineTotal, item.currencyCode)}</span>
                  </li>
                ))}
              </ul>

              {subOrder.trackingNumber && (
                <p className="mt-2 text-xs text-ink-500">
                  Guía <span className="font-mono">{subOrder.trackingNumber}</span>
                  {subOrder.carrier ? ` · ${subOrder.carrier}` : ''} ·{' '}
                  {formatDate(subOrder.shippedAt)}
                </p>
              )}
            </article>
          ))}
        </div>
      )}
    </div>
  );
}

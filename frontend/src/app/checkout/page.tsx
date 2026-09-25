'use client';

import Link from 'next/link';
import { useCallback, useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { apiFetch, errorMessage } from '@/lib/api';
import { useAuth } from '@/lib/auth';
import type { Cart, CheckoutResponse } from '@/lib/types';
import { RequireAuth } from '@/components/RequireAuth';
import { EmptyState, ErrorBox, Loading, PageHeader } from '@/components/feedback';
import { StatusBadge, formatMoney } from '@/components/ui';
import { notifyCartUpdated } from '@/components/CartCount';

export default function CheckoutPage() {
  return (
    <RequireAuth role="ROLE_BUYER">
      <CheckoutContent />
    </RequireAuth>
  );
}

function CheckoutContent() {
  const { session } = useAuth();
  const router = useRouter();
  const [cart, setCart] = useState<Cart | null>(null);
  const [loading, setLoading] = useState(true);
  const [notes, setNotes] = useState('');
  const [processing, setProcessing] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<CheckoutResponse | null>(null);

  const load = useCallback(async () => {
    if (!session?.token) return;
    setLoading(true);
    try {
      setCart(await apiFetch<Cart>('/api/v1/cart', { token: session.token }));
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [session?.token]);

  useEffect(() => {
    void load();
  }, [load]);

  async function pay() {
    if (!session?.token) return;
    setProcessing(true);
    setError(null);
    try {
      const response = await apiFetch<CheckoutResponse>('/api/v1/orders/checkout', {
        method: 'POST',
        token: session.token,
        body: { notes: notes.trim() || undefined },
      });
      setResult(response);
      notifyCartUpdated();
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setProcessing(false);
    }
  }

  if (loading) return <Loading />;

  // Resultado del pago
  if (result) {
    const order = result.order;
    return (
      <div className="mx-auto max-w-3xl">
        <div
          className={`card p-6 ${result.paymentCaptured ? 'border-green-200' : 'border-amber-200'}`}
        >
          <div className="flex items-center gap-3">
            <span className="text-3xl">{result.paymentCaptured ? '✅' : '⚠️'}</span>
            <div>
              <h1 className="text-lg font-bold text-ink-900">
                {result.paymentCaptured ? 'Pago confirmado' : 'Pago no completado'}
              </h1>
              <p className="text-sm text-ink-500">{result.paymentMessage}</p>
            </div>
          </div>

          <dl className="mt-5 grid gap-3 sm:grid-cols-2">
            <div>
              <dt className="text-xs uppercase tracking-wide text-ink-500">Orden</dt>
              <dd className="font-mono text-sm font-semibold">{order.orderNumber}</dd>
            </div>
            <div>
              <dt className="text-xs uppercase tracking-wide text-ink-500">Estado</dt>
              <dd>
                <StatusBadge status={order.status} />
              </dd>
            </div>
            <div>
              <dt className="text-xs uppercase tracking-wide text-ink-500">Total cobrado</dt>
              <dd className="text-lg font-bold">
                {formatMoney(result.chargedAmount, order.currencyCode)}
              </dd>
            </div>
            <div>
              <dt className="text-xs uppercase tracking-wide text-ink-500">Referencia de pago</dt>
              <dd className="break-all font-mono text-xs">{result.paymentReference ?? '—'}</dd>
            </div>
          </dl>

          <div className="mt-5">
            <h2 className="text-sm font-bold text-ink-800">
              Sub-órdenes por vendedor ({order.subOrders.length})
            </h2>
            <div className="mt-2 space-y-2">
              {order.subOrders.map((subOrder) => (
                <div key={subOrder.subOrderId} className="rounded-lg border border-ink-200 p-3">
                  <div className="flex flex-wrap items-center justify-between gap-2">
                    <span className="font-mono text-xs text-ink-500">
                      vendedor {subOrder.sellerId.slice(0, 8)}…
                    </span>
                    <div className="flex items-center gap-2">
                      <StatusBadge status={subOrder.status} />
                      <span className="text-sm font-semibold">
                        {formatMoney(subOrder.subtotal, subOrder.currencyCode)}
                      </span>
                    </div>
                  </div>
                  <ul className="mt-2 space-y-1 text-sm text-ink-600">
                    {subOrder.items.map((item) => (
                      <li key={item.orderItemId} className="flex justify-between">
                        <span>
                          {item.productName} × {item.quantity}
                        </span>
                        <span>{formatMoney(item.lineTotal, item.currencyCode)}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              ))}
            </div>
          </div>

          <div className="mt-6 flex flex-wrap gap-3">
            <Link href="/mis-compras" className="btn btn-primary">
              Ver mis compras
            </Link>
            <Link href="/productos" className="btn btn-secondary">
              Seguir comprando
            </Link>
            <button type="button" className="btn btn-secondary" onClick={() => router.refresh()}>
              Actualizar
            </button>
          </div>
        </div>
      </div>
    );
  }

  const items = cart?.items ?? [];

  if (items.length === 0) {
    return (
      <EmptyState
        title="No hay nada que pagar"
        description="Tu carrito está vacío."
        action={
          <Link href="/productos" className="btn btn-primary">
            Ir al catálogo
          </Link>
        }
      />
    );
  }

  const sellers = new Set(items.map((item) => item.sellerId));

  return (
    <div>
      <PageHeader title="Confirmar compra" description="Revisa el pedido antes de pagar." />

      {error && (
        <div className="mb-4">
          <ErrorBox>{error}</ErrorBox>
        </div>
      )}

      <div className="grid gap-6 lg:grid-cols-3">
        <div className="card overflow-hidden lg:col-span-2">
          <table className="table">
            <thead>
              <tr>
                <th>Producto</th>
                <th className="w-24">Cantidad</th>
                <th className="w-32">Subtotal</th>
              </tr>
            </thead>
            <tbody>
              {items.map((item) => (
                <tr key={item.itemId}>
                  <td>
                    <p className="font-medium text-ink-900">{item.productName}</p>
                    <p className="font-mono text-[11px] text-ink-400">
                      vendedor {item.sellerId.slice(0, 8)}…
                    </p>
                  </td>
                  <td>{item.quantity}</td>
                  <td className="font-semibold">{formatMoney(item.lineTotal, item.currencyCode)}</td>
                </tr>
              ))}
            </tbody>
          </table>

          <div className="border-t border-ink-100 p-4">
            <label className="label" htmlFor="notes">
              Nota para el pedido (opcional)
            </label>
            <textarea
              id="notes"
              className="textarea"
              rows={2}
              value={notes}
              onChange={(event) => setNotes(event.target.value)}
              placeholder="Por ejemplo: entregar por la tarde"
            />
          </div>
        </div>

        <aside className="card h-fit p-5">
          <h2 className="font-bold text-ink-900">Resumen</h2>
          <dl className="mt-3 space-y-2 text-sm">
            <div className="flex justify-between">
              <dt className="text-ink-600">Artículos</dt>
              <dd className="font-semibold">{cart?.totalItemCount ?? 0}</dd>
            </div>
            <div className="flex justify-between">
              <dt className="text-ink-600">Vendedores implicados</dt>
              <dd className="font-semibold">{sellers.size}</dd>
            </div>
            <div className="flex justify-between border-t border-ink-100 pt-2 text-base">
              <dt className="font-semibold text-ink-900">Total</dt>
              <dd className="font-bold text-ink-900">
                {formatMoney(cart?.subtotal ?? 0, cart?.currencyCode)}
              </dd>
            </div>
          </dl>

          <button
            type="button"
            className="btn btn-primary mt-4 w-full"
            disabled={processing}
            onClick={() => void pay()}
          >
            {processing ? 'Procesando pago…' : 'Pagar ahora'}
          </button>

          <p className="mt-3 text-xs text-ink-500">
            El stock se reserva con bloqueo pesimista antes de cobrar, así que dos compradores no
            pueden llevarse la misma unidad.
          </p>
        </aside>
      </div>
    </div>
  );
}

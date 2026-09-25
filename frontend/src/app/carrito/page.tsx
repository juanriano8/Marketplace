'use client';

import Link from 'next/link';
import { useCallback, useEffect, useState } from 'react';
import { apiFetch, errorMessage } from '@/lib/api';
import { useAuth } from '@/lib/auth';
import type { Cart } from '@/lib/types';
import { RequireAuth } from '@/components/RequireAuth';
import { EmptyState, ErrorBox, Loading, PageHeader } from '@/components/feedback';
import { formatMoney } from '@/components/ui';
import { notifyCartUpdated } from '@/components/CartCount';

export default function CartPage() {
  return (
    <RequireAuth role="ROLE_BUYER">
      <CartContent />
    </RequireAuth>
  );
}

function CartContent() {
  const { session } = useAuth();
  const [cart, setCart] = useState<Cart | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState<string | null>(null);

  const load = useCallback(async () => {
    if (!session?.token) return;
    setLoading(true);
    setError(null);
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

  async function changeQuantity(productId: string, quantity: number) {
    if (!session?.token) return;
    setBusy(productId);
    setError(null);
    try {
      const updated = await apiFetch<Cart>(`/api/v1/cart/items/${productId}`, {
        method: 'PUT',
        token: session.token,
        body: { quantity },
      });
      setCart(updated);
      notifyCartUpdated();
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setBusy(null);
    }
  }

  async function removeItem(productId: string) {
    if (!session?.token) return;
    setBusy(productId);
    setError(null);
    try {
      const updated = await apiFetch<Cart>(`/api/v1/cart/items/${productId}`, {
        method: 'DELETE',
        token: session.token,
      });
      setCart(updated);
      notifyCartUpdated();
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setBusy(null);
    }
  }

  async function clearCart() {
    if (!session?.token) return;
    setBusy('all');
    setError(null);
    try {
      const updated = await apiFetch<Cart>('/api/v1/cart', {
        method: 'DELETE',
        token: session.token,
      });
      setCart(updated);
      notifyCartUpdated();
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setBusy(null);
    }
  }

  if (loading) return <Loading />;
  if (error && !cart) return <ErrorBox>{error}</ErrorBox>;

  const items = cart?.items ?? [];

  return (
    <div>
      <PageHeader
        title="Mi carrito"
        description={cart ? `${cart.totalItemCount} artículo(s)` : undefined}
        action={
          items.length > 0 ? (
            <button
              type="button"
              className="btn btn-secondary btn-sm"
              onClick={() => void clearCart()}
              disabled={busy === 'all'}
            >
              Vaciar carrito
            </button>
          ) : undefined
        }
      />

      {error && (
        <div className="mb-4">
          <ErrorBox>{error}</ErrorBox>
        </div>
      )}

      {items.length === 0 ? (
        <EmptyState
          title="Tu carrito está vacío"
          description="Añade productos desde el catálogo para continuar."
          action={
            <Link href="/productos" className="btn btn-primary">
              Ir al catálogo
            </Link>
          }
        />
      ) : (
        <div className="grid gap-6 lg:grid-cols-3">
          <div className="card overflow-hidden lg:col-span-2">
            <table className="table">
              <thead>
                <tr>
                  <th>Producto</th>
                  <th className="w-32">Precio</th>
                  <th className="w-36">Cantidad</th>
                  <th className="w-32">Subtotal</th>
                  <th className="w-16" />
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
                    <td>{formatMoney(item.unitPrice, item.currencyCode)}</td>
                    <td>
                      <div className="flex items-center gap-1">
                        <button
                          type="button"
                          className="btn btn-secondary btn-sm"
                          disabled={busy === item.productId || item.quantity <= 1}
                          onClick={() => void changeQuantity(item.productId, item.quantity - 1)}
                        >
                          −
                        </button>
                        <span className="w-10 text-center font-semibold">{item.quantity}</span>
                        <button
                          type="button"
                          className="btn btn-secondary btn-sm"
                          disabled={busy === item.productId}
                          onClick={() => void changeQuantity(item.productId, item.quantity + 1)}
                        >
                          +
                        </button>
                      </div>
                    </td>
                    <td className="font-semibold">
                      {formatMoney(item.lineTotal, item.currencyCode)}
                    </td>
                    <td>
                      <button
                        type="button"
                        className="text-sm text-red-600 hover:underline"
                        disabled={busy === item.productId}
                        onClick={() => void removeItem(item.productId)}
                        title="Quitar del carrito"
                      >
                        Quitar
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <aside className="card h-fit p-5">
            <h2 className="font-bold text-ink-900">Resumen</h2>
            <dl className="mt-3 space-y-2 text-sm">
              <div className="flex justify-between">
                <dt className="text-ink-600">Artículos</dt>
                <dd className="font-semibold">{cart?.totalItemCount ?? 0}</dd>
              </div>
              <div className="flex justify-between border-t border-ink-100 pt-2 text-base">
                <dt className="font-semibold text-ink-900">Total</dt>
                <dd className="font-bold text-ink-900">
                  {formatMoney(cart?.subtotal ?? 0, cart?.currencyCode)}
                </dd>
              </div>
            </dl>

            <Link href="/checkout" className="btn btn-primary mt-4 w-full">
              Continuar al pago
            </Link>

            <p className="mt-3 text-xs text-ink-500">
              Al confirmar se creará una orden por cada vendedor distinto y se cobrará con la
              pasarela simulada.
            </p>
          </aside>
        </div>
      )}
    </div>
  );
}

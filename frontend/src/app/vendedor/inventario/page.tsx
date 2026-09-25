'use client';

import Link from 'next/link';
import { useCallback, useEffect, useState } from 'react';
import { apiFetch, errorMessage, type Page } from '@/lib/api';
import { useAuth } from '@/lib/auth';
import type { Product, SellerStock } from '@/lib/types';
import { RequireAuth } from '@/components/RequireAuth';
import { EmptyState, ErrorBox, Loading, PageHeader, Success, Warning } from '@/components/feedback';
import { formatDate } from '@/components/ui';

export default function SellerInventoryPage() {
  return (
    <RequireAuth role="ROLE_SELLER">
      <SellerInventory />
    </RequireAuth>
  );
}

type Mode = 'delta' | 'absolute';

function SellerInventory() {
  const { session } = useAuth();
  const [stock, setStock] = useState<SellerStock[]>([]);
  const [products, setProducts] = useState<Record<string, Product>>({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [feedback, setFeedback] = useState<{ tone: 'ok' | 'error'; text: string } | null>(null);

  const [adjusting, setAdjusting] = useState<SellerStock | null>(null);
  const [mode, setMode] = useState<Mode>('delta');
  const [amount, setAmount] = useState('1');
  const [reason, setReason] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);

  const load = useCallback(async () => {
    if (!session?.token) return;
    setLoading(true);
    setError(null);
    try {
      const page = await apiFetch<Page<SellerStock>>(
        '/api/v1/seller/inventory?page=0&size=100&sort=createdAt,desc',
        { token: session.token },
      );
      setStock(page.content);

      // El stock devuelve productId; se necesitan los nombres del catálogo propio.
      const catalog = await apiFetch<Page<Product>>(
        '/api/v1/seller/products?page=0&size=100',
        { token: session.token },
      );
      const map: Record<string, Product> = {};
      for (const product of catalog.content) {
        map[product.id] = product;
      }
      setProducts(map);
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [session?.token]);

  useEffect(() => {
    void load();
  }, [load]);

  async function submitAdjustment(event: React.FormEvent) {
    event.preventDefault();
    if (!session?.token || !adjusting) return;
    setSubmitting(true);
    setFormError(null);
    try {
      const body: Record<string, unknown> = {
        productId: adjusting.productId,
        reason: reason.trim() || undefined,
      };
      if (mode === 'delta') {
        body.quantityDelta = Number(amount);
      } else {
        body.physicalQuantity = Number(amount);
      }

      await apiFetch<SellerStock>('/api/v1/seller/inventory/adjust', {
        method: 'POST',
        token: session.token,
        body,
      });

      setAdjusting(null);
      setReason('');
      setAmount('1');
      setFeedback({ tone: 'ok', text: 'Stock ajustado y registrado en la auditoría.' });
      await load();
    } catch (err) {
      setFormError(errorMessage(err));
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) return <Loading />;

  return (
    <div>
      <PageHeader
        title="Inventario"
        description="Ajusta las existencias físicas de tus productos. Cada cambio queda en la auditoría."
        action={
          <Link href="/vendedor" className="btn btn-secondary btn-sm">
            Ir a mi catálogo
          </Link>
        }
      />

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

      {adjusting && (
        <form onSubmit={submitAdjustment} className="card mb-6 p-5">
          <h2 className="font-bold text-ink-900">
            Ajustar stock · {products[adjusting.productId]?.name ?? adjusting.productId.slice(0, 8)}
          </h2>
          <p className="mt-1 text-xs text-ink-500">
            Disponible actual: <strong>{adjusting.availableQuantity}</strong> · reservado:{' '}
            {adjusting.reservedQuantity} · vendible: {adjusting.sellableQuantity}
          </p>

          <div className="mt-4 grid gap-3 sm:grid-cols-3">
            <div>
              <span className="label">Tipo de ajuste</span>
              <div className="flex gap-2">
                <button
                  type="button"
                  className={`btn btn-sm flex-1 ${mode === 'delta' ? 'btn-primary' : 'btn-secondary'}`}
                  onClick={() => setMode('delta')}
                >
                  Sumar / restar
                </button>
                <button
                  type="button"
                  className={`btn btn-sm flex-1 ${mode === 'absolute' ? 'btn-primary' : 'btn-secondary'}`}
                  onClick={() => setMode('absolute')}
                >
                  Recuento
                </button>
              </div>
            </div>

            <div>
              <label className="label" htmlFor="amount">
                {mode === 'delta' ? 'Unidades (usa negativo para restar)' : 'Cantidad física real'}
              </label>
              <input
                id="amount"
                type="number"
                className="input"
                value={amount}
                onChange={(event) => setAmount(event.target.value)}
                required
              />
            </div>

            <div>
              <label className="label" htmlFor="reason">
                Motivo
              </label>
              <input
                id="reason"
                className="input"
                maxLength={500}
                value={reason}
                onChange={(event) => setReason(event.target.value)}
                placeholder="Unidades dañadas, recuento trimestral…"
              />
            </div>
          </div>

          {mode === 'delta' && Number(amount) < 0 && (
            <div className="mt-3">
              <Warning>
                Se quitarán {Math.abs(Number(amount))} unidad(es). No se permite dejar el stock en
                negativo.
              </Warning>
            </div>
          )}

          {formError && (
            <div className="mt-3">
              <ErrorBox>{formError}</ErrorBox>
            </div>
          )}

          <div className="mt-4 flex gap-2">
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? 'Aplicando…' : 'Aplicar ajuste'}
            </button>
            <button type="button" className="btn btn-secondary" onClick={() => setAdjusting(null)}>
              Cancelar
            </button>
          </div>
        </form>
      )}

      {stock.length === 0 ? (
        <EmptyState
          title="No hay registros de inventario"
          description="El inventario se crea automáticamente cuando publicas un producto."
          action={
            <Link href="/vendedor" className="btn btn-primary">
              Publicar un producto
            </Link>
          }
        />
      ) : (
        <div className="card overflow-x-auto">
          <table className="table">
            <thead>
              <tr>
                <th>Producto</th>
                <th className="w-28">Disponible</th>
                <th className="w-28">Reservado</th>
                <th className="w-28">Vendible</th>
                <th className="w-28">Alerta</th>
                <th className="w-40">Último ajuste</th>
                <th className="w-32" />
              </tr>
            </thead>
            <tbody>
              {stock.map((row) => (
                <tr key={row.stockItemId}>
                  <td>
                    <p className="font-medium text-ink-900">
                      {products[row.productId]?.name ?? 'Producto'}
                    </p>
                    <p className="font-mono text-[11px] text-ink-400">{row.productId.slice(0, 8)}…</p>
                  </td>
                  <td className="font-semibold">{row.availableQuantity}</td>
                  <td className="text-ink-500">{row.reservedQuantity}</td>
                  <td className="font-semibold">{row.sellableQuantity}</td>
                  <td>
                    {row.lowStock ? (
                      <span className="badge badge-yellow">Stock bajo</span>
                    ) : (
                      <span className="badge badge-green">OK</span>
                    )}
                  </td>
                  <td className="text-xs text-ink-500">{formatDate(row.lastAdjustedAt)}</td>
                  <td>
                    <button
                      type="button"
                      className="btn btn-secondary btn-sm"
                      onClick={() => {
                        setAdjusting(row);
                        setMode('delta');
                        setAmount('1');
                        setReason('');
                        setFormError(null);
                      }}
                    >
                      Ajustar
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

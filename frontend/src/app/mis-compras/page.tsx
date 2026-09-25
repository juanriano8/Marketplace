'use client';

import Link from 'next/link';
import { useCallback, useEffect, useState } from 'react';
import { apiFetch, errorMessage, type Page } from '@/lib/api';
import { useAuth } from '@/lib/auth';
import type { Order } from '@/lib/types';
import { RequireAuth } from '@/components/RequireAuth';
import { EmptyState, ErrorBox, Loading, PageHeader } from '@/components/feedback';
import { StatusBadge, formatDate, formatMoney } from '@/components/ui';

export default function MyOrdersPage() {
  return (
    <RequireAuth role="ROLE_BUYER">
      <OrdersContent />
    </RequireAuth>
  );
}

function OrdersContent() {
  const { session } = useAuth();
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [expanded, setExpanded] = useState<string | null>(null);

  const load = useCallback(async () => {
    if (!session?.token) return;
    setLoading(true);
    setError(null);
    try {
      const page = await apiFetch<Page<Order>>(
        '/api/v1/buyer/orders?page=0&size=50&sort=createdAt,desc',
        { token: session.token },
      );
      setOrders(page.content);
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [session?.token]);

  useEffect(() => {
    void load();
  }, [load]);

  if (loading) return <Loading />;

  return (
    <div>
      <PageHeader
        title="Mis compras"
        description="Historial de tus órdenes, con el estado de cada despacho."
        action={
          <button type="button" className="btn btn-secondary btn-sm" onClick={() => void load()}>
            Actualizar
          </button>
        }
      />

      {error && (
        <div className="mb-4">
          <ErrorBox>{error}</ErrorBox>
        </div>
      )}

      {orders.length === 0 ? (
        <EmptyState
          title="Todavía no has hecho ninguna compra"
          description="Cuando pagues tu primer pedido aparecerá aquí."
          action={
            <Link href="/productos" className="btn btn-primary">
              Ir al catálogo
            </Link>
          }
        />
      ) : (
        <div className="space-y-3">
          {orders.map((order) => {
            const open = expanded === order.orderId;
            return (
              <article key={order.orderId} className="card overflow-hidden">
                <button
                  type="button"
                  className="flex w-full flex-wrap items-center justify-between gap-3 p-4 text-left hover:bg-ink-50"
                  onClick={() => setExpanded(open ? null : order.orderId)}
                >
                  <div>
                    <p className="font-mono text-sm font-semibold text-ink-900">
                      {order.orderNumber}
                    </p>
                    <p className="text-xs text-ink-500">{formatDate(order.createdAt)}</p>
                  </div>

                  <div className="flex flex-wrap items-center gap-3">
                    <span className="text-xs text-ink-500">
                      {order.subOrders.length} envío(s)
                    </span>
                    <StatusBadge status={order.status} />
                    <span className="font-bold text-ink-900">
                      {formatMoney(order.totalAmount, order.currencyCode)}
                    </span>
                    <span className="text-ink-400">{open ? '▲' : '▼'}</span>
                  </div>
                </button>

                {open && (
                  <div className="border-t border-ink-100 bg-ink-50 p-4">
                    <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
                      <div>
                        <p className="text-xs uppercase tracking-wide text-ink-500">Pagado</p>
                        <p className="text-sm">{formatDate(order.paidAt)}</p>
                      </div>
                      <div>
                        <p className="text-xs uppercase tracking-wide text-ink-500">
                          Referencia de pago
                        </p>
                        <p className="break-all font-mono text-xs">
                          {order.paymentReference ?? '—'}
                        </p>
                      </div>
                    </div>

                    <div className="mt-4 space-y-3">
                      {order.subOrders.map((subOrder, index) => (
                        <div key={subOrder.subOrderId} className="rounded-lg border border-ink-200 bg-white p-3">
                          <div className="flex flex-wrap items-center justify-between gap-2">
                            <span className="text-sm font-semibold text-ink-800">
                              Envío {index + 1} · vendedor {subOrder.sellerId.slice(0, 8)}…
                            </span>
                            <div className="flex items-center gap-2">
                              <StatusBadge status={subOrder.status} />
                              <span className="text-sm font-semibold">
                                {formatMoney(subOrder.subtotal, subOrder.currencyCode)}
                              </span>
                            </div>
                          </div>

                          {subOrder.trackingNumber && (
                            <p className="mt-2 text-xs text-ink-600">
                              Guía <span className="font-mono">{subOrder.trackingNumber}</span>
                              {subOrder.carrier ? ` · ${subOrder.carrier}` : ''} ·{' '}
                              {formatDate(subOrder.shippedAt)}
                            </p>
                          )}

                          <ul className="mt-2 space-y-1 border-t border-ink-100 pt-2 text-sm text-ink-600">
                            {subOrder.items.map((item) => (
                              <li key={item.orderItemId} className="flex justify-between">
                                <span>
                                  {item.productName} × {item.quantity}
                                </span>
                                <span>{formatMoney(item.lineTotal, item.currencyCode)}</span>
                              </li>
                            ))}
                          </ul>

                          {subOrder.status === 'SHIPPED' && (
                            <p className="mt-2 text-xs text-green-700">
                              Ya puedes dejar tu reseña de este producto desde la ficha del catálogo.
                            </p>
                          )}
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </article>
            );
          })}
        </div>
      )}
    </div>
  );
}

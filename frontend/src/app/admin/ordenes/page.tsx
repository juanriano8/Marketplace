'use client';

import { useCallback, useEffect, useState } from 'react';
import { apiFetch, errorMessage, type Page } from '@/lib/api';
import { useAuth } from '@/lib/auth';
import type { Order } from '@/lib/types';
import { RequireAuth } from '@/components/RequireAuth';
import { EmptyState, ErrorBox, Loading, PageHeader } from '@/components/feedback';
import { StatusBadge, formatDate, formatMoney } from '@/components/ui';

export default function AdminOrdersPage() {
  return (
    <RequireAuth role="ROLE_ADMIN">
      <AllOrders />
    </RequireAuth>
  );
}

function AllOrders() {
  const { session } = useAuth();
  const [orders, setOrders] = useState<Order[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [expanded, setExpanded] = useState<string | null>(null);

  const load = useCallback(async () => {
    if (!session?.token) return;
    setLoading(true);
    setError(null);
    try {
      const result = await apiFetch<Page<Order>>(
        `/api/v1/admin/orders?page=${page}&size=20&sort=createdAt,desc`,
        { token: session.token },
      );
      setOrders(result.content);
      setTotalElements(result.totalElements);
      setTotalPages(result.totalPages);
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [session?.token, page]);

  useEffect(() => {
    void load();
  }, [load]);

  const revenue = orders
    .filter((order) => order.status === 'PAID' || order.status === 'COMPLETED')
    .reduce((total, order) => total + order.totalAmount, 0);

  return (
    <div>
      <PageHeader
        title="Transacciones"
        description={`${totalElements} orden(es) en el marketplace`}
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

      <div className="mb-4 grid gap-3 sm:grid-cols-3">
        <div className="card p-4">
          <p className="text-xs uppercase tracking-wide text-ink-500">Órdenes totales</p>
          <p className="text-xl font-bold text-ink-900">{totalElements}</p>
        </div>
        <div className="card p-4">
          <p className="text-xs uppercase tracking-wide text-ink-500">Ingresos (página actual)</p>
          <p className="text-xl font-bold text-green-700">{formatMoney(revenue)}</p>
        </div>
        <div className="card p-4">
          <p className="text-xs uppercase tracking-wide text-ink-500">Envíos en esta página</p>
          <p className="text-xl font-bold text-ink-900">
            {orders.reduce((total, order) => total + order.subOrders.length, 0)}
          </p>
        </div>
      </div>

      {loading ? (
        <Loading />
      ) : orders.length === 0 ? (
        <EmptyState
          title="Todavía no hay transacciones"
          description="Cuando los compradores paguen sus pedidos aparecerán aquí."
        />
      ) : (
        <>
          <div className="card overflow-x-auto">
            <table className="table">
              <thead>
                <tr>
                  <th>Orden</th>
                  <th className="w-40">Comprador</th>
                  <th className="w-32">Total</th>
                  <th className="w-32">Estado</th>
                  <th className="w-24">Envíos</th>
                  <th className="w-40">Creada</th>
                  <th className="w-16" />
                </tr>
              </thead>
              <tbody>
                {orders.map((order) => (
                  <tr key={order.orderId}>
                    <td className="font-mono text-xs font-semibold text-ink-900">
                      {order.orderNumber}
                    </td>
                    <td className="font-mono text-xs text-ink-500">
                      {order.buyerId.slice(0, 8)}…
                    </td>
                    <td className="font-semibold">
                      {formatMoney(order.totalAmount, order.currencyCode)}
                    </td>
                    <td>
                      <StatusBadge status={order.status} />
                    </td>
                    <td>{order.subOrders.length}</td>
                    <td className="text-xs text-ink-500">{formatDate(order.createdAt)}</td>
                    <td>
                      <button
                        type="button"
                        className="btn btn-secondary btn-sm"
                        onClick={() =>
                          setExpanded(expanded === order.orderId ? null : order.orderId)
                        }
                      >
                        {expanded === order.orderId ? 'Ocultar' : 'Detalle'}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {totalPages > 1 && (
            <div className="mt-4 flex items-center justify-center gap-3">
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                disabled={page === 0}
                onClick={() => setPage((value) => Math.max(0, value - 1))}
              >
                ← Anterior
              </button>
              <span className="text-sm text-ink-600">
                Página {page + 1} de {totalPages}
              </span>
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                disabled={page + 1 >= totalPages}
                onClick={() => setPage((value) => value + 1)}
              >
                Siguiente →
              </button>
            </div>
          )}

          {expanded && (
            <div className="mt-4 card p-4">
              {(() => {
                const order = orders.find((item) => item.orderId === expanded);
                if (!order) return null;
                return (
                  <div>
                    <div className="flex flex-wrap items-center justify-between gap-2">
                      <h2 className="font-mono text-sm font-bold">{order.orderNumber}</h2>
                      <span className="text-xs text-ink-500">
                        Pago: <span className="font-mono">{order.paymentReference ?? '—'}</span> ·{' '}
                        {formatDate(order.paidAt)}
                      </span>
                    </div>

                    <div className="mt-3 space-y-3">
                      {order.subOrders.map((subOrder, index) => (
                        <div key={subOrder.subOrderId} className="rounded-lg border border-ink-200 p-3">
                          <div className="flex flex-wrap items-center justify-between gap-2">
                            <span className="text-sm font-semibold">
                              Envío {index + 1} · vendedor {subOrder.sellerId.slice(0, 8)}…
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
                          {subOrder.trackingNumber && (
                            <p className="mt-2 text-xs text-ink-500">
                              Guía <span className="font-mono">{subOrder.trackingNumber}</span>
                              {subOrder.carrier ? ` · ${subOrder.carrier}` : ''}
                            </p>
                          )}
                        </div>
                      ))}
                    </div>
                  </div>
                );
              })()}
            </div>
          )}
        </>
      )}
    </div>
  );
}

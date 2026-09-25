'use client';

import { useCallback, useEffect, useState } from 'react';
import { apiFetch, errorMessage, type Page } from '@/lib/api';
import { useAuth } from '@/lib/auth';
import type { Product, ProductStatus } from '@/lib/types';
import { RequireAuth } from '@/components/RequireAuth';
import { EmptyState, ErrorBox, Loading, PageHeader, Success } from '@/components/feedback';
import { StatusBadge, formatDate, formatMoney } from '@/components/ui';

const TABS: { value: ProductStatus; label: string }[] = [
  { value: 'PENDING_APPROVAL', label: 'Pendientes de aprobación' },
  { value: 'ACTIVE', label: 'Aprobados' },
  { value: 'REJECTED', label: 'Rechazados' },
  { value: 'INACTIVE', label: 'Inactivos' },
];

export default function AdminModerationPage() {
  return (
    <RequireAuth role="ROLE_ADMIN">
      <Moderation />
    </RequireAuth>
  );
}

function Moderation() {
  const { session } = useAuth();
  const [status, setStatus] = useState<ProductStatus>('PENDING_APPROVAL');
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [feedback, setFeedback] = useState<string | null>(null);
  const [busy, setBusy] = useState<string | null>(null);

  const load = useCallback(async () => {
    if (!session?.token) return;
    setLoading(true);
    setError(null);
    try {
      const page = await apiFetch<Page<Product>>(
        `/api/v1/admin/products?status=${status}&page=0&size=100&sort=createdAt,desc`,
        { token: session.token },
      );
      setProducts(page.content);
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [session?.token, status]);

  useEffect(() => {
    void load();
  }, [load]);

  async function moderate(product: Product, approved: boolean) {
    if (!session?.token) return;
    setBusy(product.id);
    setError(null);
    setFeedback(null);
    try {
      await apiFetch<Product>(`/api/v1/admin/products/${product.id}/approval`, {
        method: 'PATCH',
        token: session.token,
        body: { approved },
      });
      setFeedback(
        approved
          ? `"${product.name}" aprobado y publicado en el catálogo.`
          : `"${product.name}" rechazado.`,
      );
      await load();
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setBusy(null);
    }
  }

  return (
    <div>
      <PageHeader
        title="Moderación de productos"
        description="Aprueba los productos que publican los vendedores para que aparezcan en el catálogo."
        action={
          <button type="button" className="btn btn-secondary btn-sm" onClick={() => void load()}>
            Actualizar
          </button>
        }
      />

      <div className="mb-4 flex flex-wrap gap-2">
        {TABS.map((tab) => (
          <button
            key={tab.value}
            type="button"
            className={`btn btn-sm ${status === tab.value ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => setStatus(tab.value)}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {feedback && (
        <div className="mb-4">
          <Success>{feedback}</Success>
        </div>
      )}

      {error && (
        <div className="mb-4">
          <ErrorBox>{error}</ErrorBox>
        </div>
      )}

      {loading ? (
        <Loading />
      ) : products.length === 0 ? (
        <EmptyState
          title="No hay productos en este estado"
          description={
            status === 'PENDING_APPROVAL'
              ? 'Cuando un vendedor publique un producto aparecerá aquí para que lo apruebes.'
              : undefined
          }
        />
      ) : (
        <div className="card overflow-x-auto">
          <table className="table">
            <thead>
              <tr>
                <th>Producto</th>
                <th className="w-24">Vendedor</th>
                <th className="w-28">Precio</th>
                <th className="w-20">Stock</th>
                <th className="w-32">Estado</th>
                <th className="w-36">Publicado</th>
                <th className="w-48" />
              </tr>
            </thead>
            <tbody>
              {products.map((product) => (
                <tr key={product.id}>
                  <td>
                    <p className="font-medium text-ink-900">{product.name}</p>
                    <p className="text-[11px] text-ink-400">
                      {product.category ?? 'Sin categoría'} · {product.slug}
                    </p>
                  </td>
                  <td className="font-mono text-xs text-ink-500">
                    {product.sellerId.slice(0, 8)}…
                  </td>
                  <td>{formatMoney(product.price, product.currencyCode)}</td>
                  <td>{product.stockQuantity}</td>
                  <td>
                    <StatusBadge status={product.status} />
                  </td>
                  <td className="text-xs text-ink-500">{formatDate(product.createdAt)}</td>
                  <td>
                    {product.status === 'PENDING_APPROVAL' ? (
                      <div className="flex gap-2">
                        <button
                          type="button"
                          className="btn btn-success btn-sm"
                          disabled={busy === product.id}
                          onClick={() => void moderate(product, true)}
                        >
                          Aprobar
                        </button>
                        <button
                          type="button"
                          className="btn btn-danger btn-sm"
                          disabled={busy === product.id}
                          onClick={() => void moderate(product, false)}
                        >
                          Rechazar
                        </button>
                      </div>
                    ) : (
                      <span className="text-xs text-ink-400">Sin acciones</span>
                    )}
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

'use client';

import Link from 'next/link';
import { useCallback, useEffect, useState } from 'react';
import { apiFetch, errorMessage } from '@/lib/api';
import { useFavorites } from '@/lib/favorites';
import type { Product } from '@/lib/types';
import { RequireAuth } from '@/components/RequireAuth';
import { EmptyState, ErrorState, Loading, PageHeader } from '@/components/feedback';
import { StockChip, formatMoney } from '@/components/ui';

export default function FavoritesPage() {
  return (
    <RequireAuth role="ROLE_BUYER">
      <FavoritesContent />
    </RequireAuth>
  );
}

function FavoritesContent() {
  const { toggle } = useFavorites();
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setProducts(await apiFetch<Product[]>('/api/v1/favorites'));
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  async function remove(productId: string) {
    setBusy(productId);
    try {
      await toggle(productId);
      setProducts((current) => current.filter((product) => product.id !== productId));
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setBusy(null);
    }
  }

  if (loading) return <Loading />;

  return (
    <div>
      <PageHeader
        title="Favoritos"
        description="Productos que guardaste para comprar más tarde."
        action={
          <button type="button" className="btn btn-secondary btn-sm" onClick={() => void load()}>
            Actualizar
          </button>
        }
      />

      {error && <ErrorState message={error} onRetry={load} />}

      {products.length === 0 ? (
        <EmptyState
          title="Todavía no tienes favoritos"
          description="Usa el corazón de un producto para guardarlo aquí."
          action={
            <Link href="/productos" className="btn btn-primary">
              Ir al catálogo
            </Link>
          }
        />
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {products.map((product) => (
            <article key={product.id} className="card flex flex-col overflow-hidden">
              <div className="grid h-40 place-items-center bg-ink-100 text-4xl text-ink-300">
                {product.imageUrl ? (
                  // eslint-disable-next-line @next/next/no-img-element
                  <img
                    src={product.imageUrl}
                    alt={product.name}
                    className="h-full w-full object-cover"
                  />
                ) : (
                  <span>📦</span>
                )}
              </div>

              <div className="flex flex-1 flex-col p-4">
                <div className="flex items-start justify-between gap-2">
                  <h2 className="font-semibold leading-tight text-ink-900">
                    <Link href={`/productos/${product.slug}`} className="hover:text-brand-700">
                      {product.name}
                    </Link>
                  </h2>
                  {product.category && <span className="badge badge-gray">{product.category}</span>}
                </div>

                <div className="mt-3 flex items-center justify-between">
                  <span className="text-lg font-bold tabular text-ink-900">
                    {formatMoney(product.price, product.currencyCode)}
                  </span>
                  <StockChip quantity={product.stockQuantity} />
                </div>

                <div className="mt-4 flex gap-2">
                  <Link href={`/productos/${product.slug}`} className="btn btn-secondary flex-1">
                    Ver detalle
                  </Link>
                  <button
                    type="button"
                    className="btn btn-secondary"
                    disabled={busy === product.id}
                    onClick={() => void remove(product.id)}
                  >
                    {busy === product.id ? '…' : 'Quitar'}
                  </button>
                </div>
              </div>
            </article>
          ))}
        </div>
      )}
    </div>
  );
}

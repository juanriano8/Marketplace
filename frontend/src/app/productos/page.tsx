'use client';

import Link from 'next/link';
import { useCallback, useEffect, useState } from 'react';
import { apiFetch, errorMessage, type Page } from '@/lib/api';
import { useAuth } from '@/lib/auth';
import type { Product } from '@/lib/types';
import { EmptyState, ErrorBox, Loading, PageHeader } from '@/components/feedback';
import { formatMoney } from '@/components/ui';
import { notifyCartUpdated } from '@/components/CartCount';
const PAGE_SIZE = 9;

export default function CatalogPage() {
  const { session, isBuyer } = useAuth();
  const [products, setProducts] = useState<Product[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [feedback, setFeedback] = useState<{ tone: 'ok' | 'error'; text: string } | null>(null);
  const [busyProduct, setBusyProduct] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const result = await apiFetch<Page<Product>>(
        `/api/v1/products?page=${page}&size=${PAGE_SIZE}&sort=createdAt,desc`,
      );
      setProducts(result.content);
      setTotalPages(result.totalPages);
      setTotalElements(result.totalElements);
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => {
    void load();
  }, [load]);

  async function addToCart(product: Product) {
    if (!session?.token) return;
    setBusyProduct(product.id);
    setFeedback(null);
    try {
      await apiFetch('/api/v1/cart/items', {
        method: 'POST',
        token: session.token,
        body: { productId: product.id, quantity: 1 },
      });
      notifyCartUpdated();
      setFeedback({ tone: 'ok', text: `"${product.name}" añadido al carrito.` });
    } catch (err) {
      setFeedback({ tone: 'error', text: errorMessage(err) });
    } finally {
      setBusyProduct(null);
    }
  }

  return (
    <div>
      <PageHeader
        title="Catálogo"
        description={`${totalElements} producto(s) aprobados y disponibles`}
      />

      {feedback && (
        <div className="mb-4">
          {feedback.tone === 'ok' ? (
            <div className="rounded-lg border border-green-200 bg-green-50 px-3 py-2 text-sm text-green-800">
              {feedback.text}
            </div>
          ) : (
            <ErrorBox>{feedback.text}</ErrorBox>
          )}
        </div>
      )}

      {loading && <Loading />}
      {error && <ErrorBox>{error}</ErrorBox>}

      {!loading && !error && products.length === 0 && (
        <EmptyState
          title="Todavía no hay productos en el catálogo"
          description="Un administrador debe aprobar los productos que publican los vendedores para que aparezcan aquí."
          action={
            <Link href="/registro" className="btn btn-secondary">
              Crear una cuenta de vendedor
            </Link>
          }
        />
      )}

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

              {product.description && (
                <p className="mt-1 line-clamp-2 text-sm text-ink-500">{product.description}</p>
              )}

              <div className="mt-3 flex items-center justify-between">
                <span className="text-lg font-bold text-ink-900">
                  {formatMoney(product.price, product.currencyCode)}
                </span>
                <span className="text-xs text-ink-500">
                  {product.stockQuantity > 0
                    ? `${product.stockQuantity} disponibles`
                    : 'Agotado'}
                </span>
              </div>

              <div className="mt-4 flex gap-2">
                <Link href={`/productos/${product.slug}`} className="btn btn-secondary flex-1">
                  Ver detalle
                </Link>
                {isBuyer && (
                  <button
                    type="button"
                    className="btn btn-primary flex-1"
                    disabled={product.stockQuantity <= 0 || busyProduct === product.id}
                    onClick={() => void addToCart(product)}
                  >
                    {busyProduct === product.id ? '…' : 'Añadir'}
                  </button>
                )}
              </div>
            </div>
          </article>
        ))}
      </div>

      {totalPages > 1 && (
        <div className="mt-6 flex items-center justify-center gap-3">
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

      {!session && (
        <p className="mt-6 text-center text-sm text-ink-500">
          <Link href="/login" className="font-semibold text-brand-600 hover:underline">
            Inicia sesión como comprador
          </Link>{' '}
          para añadir productos al carrito.
        </p>
      )}
    </div>
  );
}

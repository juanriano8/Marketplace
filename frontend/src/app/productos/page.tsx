'use client';

import Link from 'next/link';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { Search, SlidersHorizontal, X } from 'lucide-react';
import { apiFetch, errorMessage, type Page } from '@/lib/api';
import { useAuth } from '@/lib/auth';
import type { Product } from '@/lib/types';
import { EmptyState, ErrorState, ErrorBox, PageHeader, Skeleton } from '@/components/feedback';
import { FavoriteButton } from '@/components/FavoriteButton';
import { formatMoney, StockChip } from '@/components/ui';
import { notifyCartUpdated } from '@/components/CartCount';

const PAGE_SIZE = 100;

export default function CatalogPage() {
  const { session, isBuyer } = useAuth();
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [feedback, setFeedback] = useState<{ tone: 'ok' | 'error'; text: string } | null>(null);
  const [busyProduct, setBusyProduct] = useState<string | null>(null);
  const [query, setQuery] = useState('');
  const [category, setCategory] = useState<string>('all');
  const [onlyStock, setOnlyStock] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const result = await apiFetch<Page<Product>>(
        `/api/v1/products?page=0&size=${PAGE_SIZE}&sort=createdAt,desc`,
      );
      setProducts(result.content);
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  const categories = useMemo(() => {
    const unique = new Set<string>();
    products.forEach((product) => {
      if (product.category) unique.add(product.category);
    });
    return Array.from(unique).sort((a, b) => a.localeCompare(b));
  }, [products]);

  const filtered = useMemo(() => {
    const term = query.trim().toLowerCase();
    return products.filter((product) => {
      const matchesQuery =
        !term ||
        product.name.toLowerCase().includes(term) ||
        (product.description ?? '').toLowerCase().includes(term) ||
        (product.category ?? '').toLowerCase().includes(term);
      const matchesCategory = category === 'all' || product.category === category;
      const matchesStock = !onlyStock || product.stockQuantity > 0;
      return matchesQuery && matchesCategory && matchesStock;
    });
  }, [products, query, category, onlyStock]);

  const hasFilters = query.trim().length > 0 || category !== 'all' || onlyStock;

  function clearFilters() {
    setQuery('');
    setCategory('all');
    setOnlyStock(false);
  }

  async function addToCart(product: Product) {
    if (!session) return;
    setBusyProduct(product.id);
    setFeedback(null);
    try {
      await apiFetch('/api/v1/cart/items', {
        method: 'POST',
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
    <div className="grid gap-5 lg:grid-cols-[240px_minmax(0,1fr)]">
      <aside className="hidden lg:block">
        <div className="card sticky top-24 p-4">
          <div className="flex items-center justify-between">
            <p className="flex items-center gap-1.5 text-sm font-semibold text-ink-900">
              <SlidersHorizontal className="h-4 w-4 text-ink-400" />
              Filtros
            </p>
            {hasFilters && (
              <button
                type="button"
                onClick={clearFilters}
                className="text-[11px] font-medium text-brand-700 hover:underline"
              >
                Limpiar
              </button>
            )}
          </div>

          <label className="label mt-4" htmlFor="catalog-search">
            Buscar
          </label>
          <div className="relative">
            <Search className="pointer-events-none absolute left-2.5 top-1/2 h-4 w-4 -translate-y-1/2 text-ink-400" />
            <input
              id="catalog-search"
              className="input pl-8"
              placeholder="Nombre, categoría…"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
            />
            {query && (
              <button
                type="button"
                className="absolute right-2 top-1/2 -translate-y-1/2 rounded p-0.5 text-ink-400 hover:text-ink-700"
                onClick={() => setQuery('')}
                aria-label="Borrar búsqueda"
              >
                <X className="h-3.5 w-3.5" />
              </button>
            )}
          </div>

          <p className="label mt-4">Categoría</p>
          <div className="max-h-64 space-y-0.5 overflow-y-auto pr-1">
            <button
              type="button"
              onClick={() => setCategory('all')}
              className={`flex w-full items-center justify-between rounded-md px-2 py-1.5 text-left text-xs ${
                category === 'all'
                  ? 'bg-brand-50 font-semibold text-brand-700'
                  : 'text-ink-600 hover:bg-ink-50'
              }`}
            >
              Todas
              <span className="text-[10px] text-ink-400">{products.length}</span>
            </button>
            {categories.map((item) => {
              const count = products.filter((product) => product.category === item).length;
              return (
                <button
                  key={item}
                  type="button"
                  onClick={() => setCategory(item)}
                  className={`flex w-full items-center justify-between rounded-md px-2 py-1.5 text-left text-xs ${
                    category === item
                      ? 'bg-brand-50 font-semibold text-brand-700'
                      : 'text-ink-600 hover:bg-ink-50'
                  }`}
                >
                  <span className="truncate">{item}</span>
                  <span className="text-[10px] text-ink-400">{count}</span>
                </button>
              );
            })}
          </div>

          <label className="mt-4 flex cursor-pointer items-center gap-2 text-xs text-ink-600">
            <input
              type="checkbox"
              checked={onlyStock}
              onChange={(event) => setOnlyStock(event.target.checked)}
              className="h-4 w-4 rounded border-ink-300 text-brand-600 focus:ring-brand-600"
            />
            Solo con existencias
          </label>
        </div>
      </aside>

      <section>
        <PageHeader
          title="Catálogo"
          description={`${filtered.length} de ${products.length} producto(s) visibles`}
          action={
            isBuyer ? (
              <Link href="/favoritos" className="btn btn-secondary btn-sm">
                Ver favoritos
              </Link>
            ) : undefined
          }
        />

        <div className="mb-4 flex flex-col gap-2 sm:flex-row lg:hidden">
          <div className="relative flex-1">
            <Search className="pointer-events-none absolute left-2.5 top-1/2 h-4 w-4 -translate-y-1/2 text-ink-400" />
            <input
              className="input pl-8"
              placeholder="Buscar en el catálogo…"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
            />
          </div>
          <select
            className="select sm:w-48"
            value={category}
            onChange={(event) => setCategory(event.target.value)}
            aria-label="Filtrar por categoría"
          >
            <option value="all">Todas las categorías</option>
            {categories.map((item) => (
              <option key={item} value={item}>
                {item}
              </option>
            ))}
          </select>
          <label className="flex items-center gap-2 rounded-lg border border-ink-200 bg-white px-3 py-2 text-xs text-ink-600">
            <input
              type="checkbox"
              checked={onlyStock}
              onChange={(event) => setOnlyStock(event.target.checked)}
              className="h-4 w-4 rounded border-ink-300 text-brand-600 focus:ring-brand-600"
            />
            Con stock
          </label>
        </div>

        {feedback && (
          <div className="mb-4">
            {feedback.tone === 'ok' ? (
              <div className="rounded-lg border border-brand-200 bg-brand-50 px-3 py-2 text-sm text-brand-800">
                {feedback.text}
              </div>
            ) : (
              <ErrorBox>{feedback.text}</ErrorBox>
            )}
          </div>
        )}

        {loading && (
          <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
            {Array.from({ length: 8 }).map((_, index) => (
              <Skeleton key={index} className="h-80" />
            ))}
          </div>
        )}

        {!loading && error && <ErrorState message={error} onRetry={load} />}

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

        {!loading && !error && products.length > 0 && filtered.length === 0 && (
          <EmptyState
            title="No hay productos con esos filtros"
            description="Prueba con otra búsqueda o quita los filtros."
            action={
              <button type="button" className="btn btn-secondary" onClick={clearFilters}>
                Limpiar filtros
              </button>
            }
          />
        )}

        {!loading && !error && filtered.length > 0 && (
          <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
            {filtered.map((product) => (
              <article key={product.id} className="card card-hover flex flex-col overflow-hidden">
                <div className="relative grid aspect-[4/3] place-items-center overflow-hidden bg-ink-100 text-4xl text-ink-300">
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
                  <FavoriteButton
                    productId={product.id}
                    className="absolute right-2 top-2 bg-white/90"
                  />
                </div>

                <div className="flex flex-1 flex-col p-3.5">
                  <div className="flex items-start justify-between gap-2">
                    <h2 className="line-clamp-2 text-sm font-semibold leading-tight text-ink-900">
                      <Link href={`/productos/${product.slug}`} className="hover:text-brand-700">
                        {product.name}
                      </Link>
                    </h2>
                    {product.category && (
                      <span className="badge badge-gray shrink-0">{product.category}</span>
                    )}
                  </div>

                  {product.description && (
                    <p className="mt-1 line-clamp-2 text-xs text-ink-500">{product.description}</p>
                  )}

                  <div className="mt-3 flex items-center justify-between">
                    <span className="tabular text-lg font-semibold text-ink-900">
                      {formatMoney(product.price, product.currencyCode)}
                    </span>
                    <StockChip quantity={product.stockQuantity} />
                  </div>

                  <div className="mt-3 flex gap-2">
                    <Link
                      href={`/productos/${product.slug}`}
                      className="btn btn-secondary btn-sm flex-1"
                    >
                      Ver detalle
                    </Link>
                    {isBuyer && (
                      <button
                        type="button"
                        className="btn btn-primary btn-sm flex-1"
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
        )}
      </section>
    </div>
  );
}

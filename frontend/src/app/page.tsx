'use client';

import Link from 'next/link';
import { useCallback, useEffect, useState } from 'react';
import { ArrowRight, CheckCircle2, ShieldCheck, ShoppingBag, Sparkles, Store } from 'lucide-react';
import { apiFetch, errorMessage } from '@/lib/api';
import type { Product } from '@/lib/types';
import { EmptyState, ErrorState, Skeleton } from '@/components/feedback';
import { FavoriteButton } from '@/components/FavoriteButton';
import { formatMoney } from '@/components/ui';

const ROLE_CARDS = [
  {
    href: '/productos',
    title: 'Comprador',
    description: 'Explora el catálogo, guarda favoritos, paga y deja reseñas verificadas.',
    icon: ShoppingBag,
    accent: 'bg-brand-50 text-brand-700',
  },
  {
    href: '/vendedor',
    title: 'Vendedor',
    description: 'Publica productos, controla inventario y registra despachos.',
    icon: Store,
    accent: 'bg-warning-soft text-warning',
  },
  {
    href: '/admin',
    title: 'Administrador',
    description: 'Modera vendedores y productos, audita stock y revisa transacciones.',
    icon: ShieldCheck,
    accent: 'bg-ink-100 text-ink-700',
  },
];

export default function HomePage() {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const result = await apiFetch<Product[]>('/api/v1/products/ranking?limit=3');
      setProducts(result);
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  return (
    <div className="space-y-10">
      <section className="card overflow-hidden">
        <div className="grid lg:grid-cols-[1.5fr_1fr]">
          <div className="p-6 sm:p-10">
            <p className="flex items-center gap-1.5 text-[10px] font-semibold uppercase tracking-[0.16em] text-brand-700">
              <Sparkles className="h-3.5 w-3.5" />
              Marketplace multi-rol · Spring Boot + Next.js
            </p>
            <h1 className="mt-3 max-w-2xl text-3xl font-semibold tracking-tight text-ink-900 sm:text-4xl">
              Un solo panel para comprar, vender y operar el marketplace.
            </h1>
            <p className="mt-3 max-w-2xl text-sm leading-6 text-ink-500">
              Catálogo con stock real, carrito y checkout, órdenes divididas por vendedor,
              inventario auditado y moderación. Backend hexagonal con JWT y PostgreSQL en Neon.
            </p>
            <div className="mt-6 flex flex-wrap gap-3">
              <Link href="/productos" className="btn btn-primary">
                Ver catálogo
                <ArrowRight className="h-4 w-4" />
              </Link>
              <Link href="/registro" className="btn btn-secondary">
                Crear una cuenta
              </Link>
            </div>

            <div className="mt-8 grid max-w-lg grid-cols-3 gap-3 border-t border-ink-100 pt-5">
              <div>
                <p className="text-xl font-semibold tabular text-ink-900">3</p>
                <p className="text-[11px] text-ink-500">roles operativos</p>
              </div>
              <div>
                <p className="text-xl font-semibold tabular text-ink-900">44</p>
                <p className="text-[11px] text-ink-500">operaciones REST</p>
              </div>
              <div>
                <p className="text-xl font-semibold tabular text-ink-900">17</p>
                <p className="text-[11px] text-ink-500">vistas del panel</p>
              </div>
            </div>
          </div>

          <div className="hidden border-l border-ink-100 bg-brand-50 p-6 lg:flex lg:flex-col lg:justify-between">
            <div>
              <p className="text-[10px] font-semibold uppercase tracking-[0.16em] text-brand-700">
                Flujo operativo
              </p>
              <ul className="mt-4 space-y-3">
                {[
                  'Catálogo y stock en tiempo real',
                  'Checkout con pago simulado',
                  'Sub-órdenes y despachos por vendedor',
                  'Moderación y auditoría de inventario',
                ].map((item) => (
                  <li key={item} className="flex items-start gap-2 text-sm text-ink-700">
                    <CheckCircle2 className="mt-0.5 h-4 w-4 shrink-0 text-brand-600" />
                    {item}
                  </li>
                ))}
              </ul>
            </div>
            <div className="rounded-xl border border-brand-200 bg-white/70 p-4">
              <p className="text-xs font-semibold text-brand-800">Diseño Stitch</p>
              <p className="mt-1 text-[11px] leading-5 text-ink-500">
                Paleta «Origen», tipografía Inter, densidad operativa y componentes de 8–12 px.
              </p>
            </div>
          </div>
        </div>
      </section>

      <section>
        <div className="mb-4 flex items-end justify-between gap-3">
          <div>
            <p className="text-[10px] font-semibold uppercase tracking-[0.16em] text-brand-700">
              Accesos
            </p>
            <h2 className="mt-0.5 text-lg font-semibold tracking-tight text-ink-900">
              Tres roles, tres paneles
            </h2>
          </div>
        </div>
        <div className="grid gap-4 md:grid-cols-3">
          {ROLE_CARDS.map((card) => {
            const Icon = card.icon;
            return (
              <Link
                key={card.href}
                href={card.href}
                className="card card-hover group flex flex-col p-5"
              >
                <span className={`grid h-10 w-10 place-items-center rounded-lg ${card.accent}`}>
                  <Icon className="h-5 w-5" />
                </span>
                <h3 className="mt-3 flex items-center gap-1 text-sm font-semibold text-ink-900">
                  {card.title}
                  <ArrowRight className="h-3.5 w-3.5 text-ink-300 transition group-hover:translate-x-0.5 group-hover:text-brand-600" />
                </h3>
                <p className="mt-1 text-xs leading-5 text-ink-500">{card.description}</p>
              </Link>
            );
          })}
        </div>
      </section>

      <section>
        <div className="mb-4 flex items-end justify-between gap-3">
          <h2 className="text-lg font-bold text-ink-900">Productos destacados</h2>
          <Link href="/productos" className="text-sm font-semibold text-brand-600 hover:underline">
            Ver todo
          </Link>
        </div>

        {loading && (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {[0, 1, 2].map((index) => (
              <Skeleton key={index} className="h-60" />
            ))}
          </div>
        )}

        {!loading && error && <ErrorState message={error} onRetry={load} />}

        {!loading && !error && products.length === 0 && (
          <EmptyState
            title="Todavía no hay productos destacados"
            description="Cuando un administrador apruebe productos, aparecerán aquí."
            action={
              <Link href="/productos" className="btn btn-secondary">
                Ver catálogo
              </Link>
            }
          />
        )}

        {!loading && !error && products.length > 0 && (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {products.map((product) => (
              <Link
                key={product.id}
                href={`/productos/${product.slug}`}
                className="card relative flex flex-col overflow-hidden transition hover:-translate-y-0.5 hover:shadow-md"
              >
                <FavoriteButton
                  productId={product.id}
                  className="absolute right-2 top-2 z-10 bg-white/90"
                />
                <div className="grid h-36 place-items-center bg-ink-100 text-3xl text-ink-300">
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
                  <h3 className="font-semibold leading-tight text-ink-900">{product.name}</h3>
                  {product.description && (
                    <p className="mt-1 line-clamp-2 text-sm text-ink-500">{product.description}</p>
                  )}
                  <span className="mt-3 text-lg font-bold text-brand-600">
                    {formatMoney(product.price, product.currencyCode)}
                  </span>
                </div>
              </Link>
            ))}
          </div>
        )}
      </section>

      <section className="grid gap-4 md:grid-cols-2">
        <div className="card p-5">
          <h3 className="font-bold text-ink-900">Qué se puede hacer aquí</h3>
          <ul className="mt-2 space-y-1 text-sm text-ink-600">
            <li>· Registro e inicio de sesión con JWT por rol</li>
            <li>· Catálogo público con paginación y stock real</li>
            <li>· Carrito, checkout y pago (pasarela simulada)</li>
            <li>· Órdenes divididas en sub-órdenes por vendedor</li>
            <li>· Reseñas solo para compras verificadas</li>
            <li>· Moderación de productos y verificación de vendedores</li>
            <li>· Auditoría del ledger de inventario</li>
          </ul>
        </div>
        <div className="card p-5">
          <h3 className="font-bold text-ink-900">Cómo probarlo</h3>
          <ol className="mt-2 space-y-1 text-sm text-ink-600">
            <li>1. Entra como administrador con las credenciales del backend (<code>.env</code>).</li>
            <li>2. Registra un vendedor y apruébalo desde el panel de administración.</li>
            <li>3. Inicia sesión como vendedor y publica un producto.</li>
            <li>4. Apruébalo como administrador para que aparezca en el catálogo.</li>
            <li>5. Regístrate como comprador, añádelo al carrito y paga.</li>
            <li>6. Como vendedor, registra el despacho; como comprador, deja tu reseña.</li>
          </ol>
          <p className="mt-3 text-xs text-ink-500">
            La API se consume a través del proxy de Next (<code>/api/v1/**</code>).
          </p>
        </div>
      </section>
    </div>
  );
}

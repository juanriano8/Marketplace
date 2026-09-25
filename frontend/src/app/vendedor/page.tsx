'use client';

import { useCallback, useEffect, useState } from 'react';
import { apiFetch, errorMessage, type Page } from '@/lib/api';
import { useAuth } from '@/lib/auth';
import type { CreateProductBody, Product } from '@/lib/types';
import { RequireAuth } from '@/components/RequireAuth';
import { EmptyState, ErrorBox, Info, Loading, PageHeader, Warning } from '@/components/feedback';
import { ProductForm } from '@/components/ProductForm';
import { StatusBadge, formatDate, formatMoney } from '@/components/ui';

export default function SellerCatalogPage() {
  return (
    <RequireAuth role="ROLE_SELLER">
      <SellerCatalog />
    </RequireAuth>
  );
}

function SellerCatalog() {
  const { session } = useAuth();
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [feedback, setFeedback] = useState<{ tone: 'ok' | 'error'; text: string } | null>(null);

  const [creating, setCreating] = useState(false);
  const [editing, setEditing] = useState<Product | null>(null);
  const [formError, setFormError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const load = useCallback(async () => {
    if (!session?.token) return;
    setLoading(true);
    setError(null);
    try {
      const page = await apiFetch<Page<Product>>(
        '/api/v1/seller/products?page=0&size=100&sort=createdAt,desc',
        { token: session.token },
      );
      setProducts(page.content);
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [session?.token]);

  useEffect(() => {
    void load();
  }, [load]);

  async function createProduct(body: CreateProductBody) {
    if (!session?.token) return;
    setSubmitting(true);
    setFormError(null);
    try {
      await apiFetch<Product>('/api/v1/seller/products', {
        method: 'POST',
        token: session.token,
        body,
      });
      setCreating(false);
      setFeedback({
        tone: 'ok',
        text: 'Producto publicado. Queda pendiente hasta que un administrador lo apruebe.',
      });
      await load();
    } catch (err) {
      setFormError(errorMessage(err));
    } finally {
      setSubmitting(false);
    }
  }

  async function updateProduct(product: Product, body: CreateProductBody) {
    if (!session?.token) return;
    setSubmitting(true);
    setFormError(null);
    try {
      await apiFetch<Product>(`/api/v1/seller/products/${product.id}`, {
        method: 'PUT',
        token: session.token,
        body: {
          name: body.name,
          description: body.description,
          price: body.price,
          category: body.category,
          imageUrl: body.imageUrl,
        },
      });
      setEditing(null);
      setFeedback({ tone: 'ok', text: 'Producto actualizado.' });
      await load();
    } catch (err) {
      setFormError(errorMessage(err));
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) return <Loading />;

  const approved = session?.sellerApproved === true;

  return (
    <div>
      <PageHeader
        title="Mi catálogo"
        description={`${products.length} producto(s) publicados`}
        action={
          !creating &&
          !editing && (
            <button
              type="button"
              className="btn btn-primary"
              onClick={() => {
                setCreating(true);
                setFormError(null);
              }}
            >
              + Nuevo producto
            </button>
          )
        }
      />

      {!approved && (
        <div className="mb-4">
          <Warning>
            Tu cuenta de vendedor <strong>todavía no está verificada</strong>. Un administrador debe
            aprobarla antes de que puedas publicar productos.
          </Warning>
        </div>
      )}

      {feedback && (
        <div className="mb-4">
          {feedback.tone === 'ok' ? (
            <Info>{feedback.text}</Info>
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

      {creating && (
        <div className="mb-6">
          <ProductForm
            submitting={submitting}
            error={formError}
            onSubmit={(body) => void createProduct(body)}
            onCancel={() => setCreating(false)}
          />
        </div>
      )}

      {editing && (
        <div className="mb-6">
          <ProductForm
            initial={editing}
            submitting={submitting}
            error={formError}
            onSubmit={(body) => void updateProduct(editing, body)}
            onCancel={() => setEditing(null)}
          />
        </div>
      )}

      {products.length === 0 ? (
        <EmptyState
          title="Todavía no has publicado productos"
          description="Crea tu primer producto; quedará pendiente hasta que el administrador lo apruebe."
        />
      ) : (
        <div className="card overflow-x-auto">
          <table className="table">
            <thead>
              <tr>
                <th>Producto</th>
                <th className="w-28">Precio</th>
                <th className="w-24">Stock</th>
                <th className="w-32">Estado</th>
                <th className="w-36">Creado</th>
                <th className="w-32" />
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
                  <td>{formatMoney(product.price, product.currencyCode)}</td>
                  <td>
                    <span
                      className={
                        product.stockQuantity === 0
                          ? 'font-semibold text-red-600'
                          : product.stockQuantity <= 5
                            ? 'font-semibold text-amber-600'
                            : ''
                      }
                    >
                      {product.stockQuantity}
                    </span>
                  </td>
                  <td>
                    <StatusBadge status={product.status} />
                  </td>
                  <td className="text-xs text-ink-500">{formatDate(product.createdAt)}</td>
                  <td>
                    <button
                      type="button"
                      className="btn btn-secondary btn-sm"
                      onClick={() => {
                        setEditing(product);
                        setCreating(false);
                        setFormError(null);
                      }}
                    >
                      Editar
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

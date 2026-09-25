'use client';

import Link from 'next/link';
import { useParams } from 'next/navigation';
import { useCallback, useEffect, useState } from 'react';
import { apiFetch, errorMessage } from '@/lib/api';
import { useAuth } from '@/lib/auth';
import type { Product, ProductReviews, Review } from '@/lib/types';
import { EmptyState, ErrorBox, Loading, Success } from '@/components/feedback';
import { Stars, formatDate, formatMoney } from '@/components/ui';
import { notifyCartUpdated } from '@/components/CartCount';

export default function ProductDetailPage() {
  const params = useParams<{ slug: string }>();
  const slug = params?.slug;
  const { session, isBuyer } = useAuth();

  const [product, setProduct] = useState<Product | null>(null);
  const [reviews, setReviews] = useState<ProductReviews | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [quantity, setQuantity] = useState(1);
  const [adding, setAdding] = useState(false);
  const [feedback, setFeedback] = useState<{ tone: 'ok' | 'error'; text: string } | null>(null);

  // Formulario de reseña
  const [rating, setRating] = useState(5);
  const [title, setTitle] = useState('');
  const [comment, setComment] = useState('');
  const [sendingReview, setSendingReview] = useState(false);
  const [reviewError, setReviewError] = useState<string | null>(null);
  const [reviewOk, setReviewOk] = useState<string | null>(null);

  const loadProduct = useCallback(async () => {
    if (!slug) return;
    setLoading(true);
    setError(null);
    try {
      const found = await apiFetch<Product>(`/api/v1/products/${slug}`);
      setProduct(found);
      const reviewPage = await apiFetch<ProductReviews>(
        `/api/v1/reviews/product/${found.id}?page=0&size=20&sort=createdAt,desc`,
      );
      setReviews(reviewPage);
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [slug]);

  useEffect(() => {
    void loadProduct();
  }, [loadProduct]);

  async function addToCart() {
    if (!session?.token || !product) return;
    setAdding(true);
    setFeedback(null);
    try {
      await apiFetch('/api/v1/cart/items', {
        method: 'POST',
        token: session.token,
        body: { productId: product.id, quantity },
      });
      notifyCartUpdated();
      setFeedback({ tone: 'ok', text: `Añadido al carrito (${quantity} unidad/es).` });
    } catch (err) {
      setFeedback({ tone: 'error', text: errorMessage(err) });
    } finally {
      setAdding(false);
    }
  }

  async function submitReview(event: React.FormEvent) {
    event.preventDefault();
    if (!session?.token || !product) return;
    setSendingReview(true);
    setReviewError(null);
    setReviewOk(null);
    try {
      await apiFetch<Review>('/api/v1/buyer/reviews', {
        method: 'POST',
        token: session.token,
        body: {
          productId: product.id,
          rating,
          title: title.trim() || undefined,
          comment: comment.trim() || undefined,
        },
      });
      setReviewOk('¡Gracias! Tu reseña se publicó como compra verificada.');
      setTitle('');
      setComment('');
      setRating(5);
      await loadProduct();
    } catch (err) {
      setReviewError(errorMessage(err));
    } finally {
      setSendingReview(false);
    }
  }

  if (loading) return <Loading />;
  if (error) return <ErrorBox>{error}</ErrorBox>;
  if (!product) {
    return (
      <EmptyState
        title="Producto no encontrado"
        description="Puede que ya no esté disponible en el catálogo."
        action={
          <Link href="/productos" className="btn btn-primary">
            Volver al catálogo
          </Link>
        }
      />
    );
  }

  const myReview =
    session && reviews
      ? reviews.reviews.content.find((review) => review.buyerId === session.userId)
      : undefined;

  return (
    <div className="space-y-6">
      <nav className="text-sm text-ink-500">
        <Link href="/productos" className="hover:text-brand-600">
          Catálogo
        </Link>
        <span className="mx-2">/</span>
        <span className="text-ink-700">{product.name}</span>
      </nav>

      <div className="grid gap-6 lg:grid-cols-3">
        <div className="card grid h-72 place-items-center bg-ink-100 text-6xl text-ink-300 lg:col-span-2">
          {product.imageUrl ? (
            // eslint-disable-next-line @next/next/no-img-element
            <img src={product.imageUrl} alt={product.name} className="h-full w-full object-cover" />
          ) : (
            <span>📦</span>
          )}
        </div>

        <div className="card p-5">
          <h1 className="text-xl font-bold text-ink-900">{product.name}</h1>
          {product.category && (
            <span className="badge badge-gray mt-2 inline-flex">{product.category}</span>
          )}

          <div className="mt-3 flex items-center gap-2">
            <Stars rating={reviews?.averageRating ?? 0} />
            <span className="text-sm text-ink-500">
              {reviews?.averageRating != null
                ? `${reviews.averageRating.toFixed(1)} · ${reviews.totalReviews} reseña(s)`
                : 'Sin reseñas todavía'}
            </span>
          </div>

          <p className="mt-4 text-2xl font-bold text-ink-900">
            {formatMoney(product.price, product.currencyCode)}
          </p>

          <p className="mt-1 text-sm text-ink-600">
            {product.stockQuantity > 0 ? (
              <>
                <span className="font-semibold text-green-700">{product.stockQuantity}</span>{' '}
                unidades disponibles
              </>
            ) : (
              <span className="font-semibold text-red-600">Agotado</span>
            )}
          </p>

          {isBuyer ? (
            <div className="mt-4 space-y-3">
              <div>
                <label className="label" htmlFor="quantity">
                  Cantidad
                </label>
                <input
                  id="quantity"
                  type="number"
                  min={1}
                  max={product.stockQuantity > 0 ? product.stockQuantity : 1}
                  className="input"
                  value={quantity}
                  onChange={(event) => setQuantity(Math.max(1, Number(event.target.value) || 1))}
                />
              </div>
              <button
                type="button"
                className="btn btn-primary w-full"
                disabled={adding || product.stockQuantity <= 0}
                onClick={() => void addToCart()}
              >
                {adding ? 'Añadiendo…' : 'Añadir al carrito'}
              </button>
              {feedback &&
                (feedback.tone === 'ok' ? (
                  <Success>{feedback.text}</Success>
                ) : (
                  <ErrorBox>{feedback.text}</ErrorBox>
                ))}
            </div>
          ) : (
            <p className="mt-4 rounded-lg bg-ink-50 px-3 py-2 text-xs text-ink-600">
              {session
                ? 'Solo las cuentas de comprador pueden añadir productos al carrito.'
                : 'Inicia sesión como comprador para comprar este producto.'}
            </p>
          )}

          <dl className="mt-5 space-y-1 border-t border-ink-100 pt-4 text-xs text-ink-500">
            <div className="flex justify-between">
              <dt>Vendedor</dt>
              <dd className="font-mono">{product.sellerId.slice(0, 8)}…</dd>
            </div>
            <div className="flex justify-between">
              <dt>Publicado</dt>
              <dd>{formatDate(product.createdAt)}</dd>
            </div>
            <div className="flex justify-between">
              <dt>Slug</dt>
              <dd className="font-mono">{product.slug}</dd>
            </div>
          </dl>
        </div>
      </div>

      {product.description && (
        <section className="card p-5">
          <h2 className="font-bold text-ink-900">Descripción</h2>
          <p className="mt-2 whitespace-pre-line text-sm text-ink-600">{product.description}</p>
        </section>
      )}

      <section className="card p-5">
        <div className="flex flex-wrap items-center justify-between gap-2">
          <h2 className="font-bold text-ink-900">Opiniones ({reviews?.totalReviews ?? 0})</h2>
          {reviews?.averageRating != null && (
            <span className="flex items-center gap-2 text-sm text-ink-600">
              <Stars rating={reviews.averageRating} /> {reviews.averageRating.toFixed(1)} de 5
            </span>
          )}
        </div>

        {isBuyer && !myReview && (
          <form onSubmit={submitReview} className="mt-4 rounded-lg border border-ink-200 p-4">
            <p className="text-sm font-semibold text-ink-800">Deja tu reseña</p>
            <p className="mt-1 text-xs text-ink-500">
              Solo puedes reseñar productos que hayas comprado y cuyo despacho se haya registrado.
            </p>

            <div className="mt-3 grid gap-3 sm:grid-cols-2">
              <div>
                <label className="label" htmlFor="rating">
                  Puntuación
                </label>
                <select
                  id="rating"
                  className="select"
                  value={rating}
                  onChange={(event) => setRating(Number(event.target.value))}
                >
                  {[5, 4, 3, 2, 1].map((value) => (
                    <option key={value} value={value}>
                      {'★'.repeat(value)} ({value})
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label className="label" htmlFor="title">
                  Título (opcional)
                </label>
                <input
                  id="title"
                  className="input"
                  maxLength={150}
                  value={title}
                  onChange={(event) => setTitle(event.target.value)}
                  placeholder="Excelente calidad"
                />
              </div>
            </div>

            <div className="mt-3">
              <label className="label" htmlFor="comment">
                Comentario (opcional)
              </label>
              <textarea
                id="comment"
                className="textarea"
                rows={3}
                maxLength={2000}
                value={comment}
                onChange={(event) => setComment(event.target.value)}
                placeholder="Cuenta tu experiencia con el producto…"
              />
            </div>

            {reviewError && (
              <div className="mt-3">
                <ErrorBox>{reviewError}</ErrorBox>
              </div>
            )}
            {reviewOk && (
              <div className="mt-3">
                <Success>{reviewOk}</Success>
              </div>
            )}

            <button type="submit" className="btn btn-primary mt-3" disabled={sendingReview}>
              {sendingReview ? 'Publicando…' : 'Publicar reseña'}
            </button>
          </form>
        )}

        {isBuyer && myReview && (
          <div className="mt-4">
            <Success>Ya publicaste una reseña para este producto.</Success>
          </div>
        )}

        <div className="mt-4 space-y-3">
          {reviews && reviews.reviews.content.length > 0 ? (
            reviews.reviews.content.map((review) => (
              <article key={review.reviewId} className="rounded-lg border border-ink-200 p-4">
                <div className="flex flex-wrap items-center justify-between gap-2">
                  <div className="flex items-center gap-2">
                    <Stars rating={review.rating} size="sm" />
                    {review.title && <span className="font-semibold text-ink-800">{review.title}</span>}
                  </div>
                  {review.verifiedPurchase && <span className="badge badge-green">Compra verificada</span>}
                </div>
                {review.comment && <p className="mt-2 text-sm text-ink-600">{review.comment}</p>}
                <p className="mt-2 text-xs text-ink-400">{formatDate(review.createdAt)}</p>
              </article>
            ))
          ) : (
            <p className="text-sm text-ink-500">Este producto todavía no tiene opiniones.</p>
          )}
        </div>
      </section>
    </div>
  );
}

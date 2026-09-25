'use client';

import { useEffect, useState } from 'react';
import type { CreateProductBody, Product } from '@/lib/types';

type Props = {
  initial?: Product;
  submitting: boolean;
  error: string | null;
  onSubmit: (body: CreateProductBody) => void;
  onCancel: () => void;
};

export function ProductForm({ initial, submitting, error, onSubmit, onCancel }: Props) {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [price, setPrice] = useState('');
  const [stock, setStock] = useState('10');
  const [category, setCategory] = useState('');
  const [imageUrl, setImageUrl] = useState('');
  const [currency, setCurrency] = useState('USD');

  useEffect(() => {
    setName(initial?.name ?? '');
    setDescription(initial?.description ?? '');
    setPrice(initial ? String(initial.price) : '');
    setStock(initial ? String(initial.stockQuantity) : '10');
    setCategory(initial?.category ?? '');
    setImageUrl(initial?.imageUrl ?? '');
    setCurrency(initial?.currencyCode ?? 'USD');
  }, [initial]);

  function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
    onSubmit({
      name: name.trim(),
      description: description.trim() || undefined,
      price: Number(price),
      currencyCode: currency,
      stockQuantity: Number(stock),
      category: category.trim(),
      imageUrl: imageUrl.trim() || undefined,
    });
  }

  return (
    <form onSubmit={handleSubmit} className="card p-5">
      <h2 className="font-bold text-ink-900">
        {initial ? 'Editar producto' : 'Publicar nuevo producto'}
      </h2>
      <p className="mt-1 text-xs text-ink-500">
        {initial
          ? 'El stock no se cambia aquí: se ajusta desde la pestaña de Inventario.'
          : 'El producto se crea en estado Pendiente y un administrador debe aprobarlo.'}
      </p>

      <div className="mt-4 grid gap-3 sm:grid-cols-2">
        <div className="sm:col-span-2">
          <label className="label" htmlFor="name">
            Nombre *
          </label>
          <input
            id="name"
            className="input"
            maxLength={255}
            value={name}
            onChange={(event) => setName(event.target.value)}
            required
            placeholder="Audífonos inalámbricos Pro"
          />
        </div>

        <div className="sm:col-span-2">
          <label className="label" htmlFor="description">
            Descripción
          </label>
          <textarea
            id="description"
            className="textarea"
            rows={3}
            maxLength={5000}
            value={description}
            onChange={(event) => setDescription(event.target.value)}
            placeholder="Características del producto…"
          />
        </div>

        <div>
          <label className="label" htmlFor="price">
            Precio *
          </label>
          <input
            id="price"
            type="number"
            step="0.01"
            min="0.01"
            className="input"
            value={price}
            onChange={(event) => setPrice(event.target.value)}
            required
            placeholder="149.99"
          />
        </div>

        <div>
          <label className="label" htmlFor="currency">
            Moneda
          </label>
          <select
            id="currency"
            className="select"
            value={currency}
            onChange={(event) => setCurrency(event.target.value)}
          >
            <option value="USD">USD</option>
            <option value="EUR">EUR</option>
            <option value="COP">COP</option>
            <option value="MXN">MXN</option>
          </select>
        </div>

        <div>
          <label className="label" htmlFor="category">
            Categoría *
          </label>
          <input
            id="category"
            className="input"
            value={category}
            onChange={(event) => setCategory(event.target.value)}
            required
            placeholder="Electrónica"
          />
        </div>

        <div>
          <label className="label" htmlFor="stock">
            Stock inicial *
          </label>
          <input
            id="stock"
            type="number"
            min={0}
            className="input"
            value={stock}
            onChange={(event) => setStock(event.target.value)}
            required
            disabled={Boolean(initial)}
          />
          {initial && (
            <p className="mt-1 text-[11px] text-ink-400">
              Se gestiona desde Inventario
            </p>
          )}
        </div>

        <div className="sm:col-span-2">
          <label className="label" htmlFor="imageUrl">
            URL de la imagen
          </label>
          <input
            id="imageUrl"
            className="input"
            value={imageUrl}
            onChange={(event) => setImageUrl(event.target.value)}
            placeholder="https://…"
          />
        </div>
      </div>

      {error && (
        <div className="mt-4 rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-800">
          {error}
        </div>
      )}

      <div className="mt-4 flex gap-2">
        <button type="submit" className="btn btn-primary" disabled={submitting}>
          {submitting ? 'Guardando…' : initial ? 'Guardar cambios' : 'Publicar producto'}
        </button>
        <button type="button" className="btn btn-secondary" onClick={onCancel}>
          Cancelar
        </button>
      </div>
    </form>
  );
}

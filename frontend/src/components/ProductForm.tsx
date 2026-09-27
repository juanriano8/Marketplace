'use client';

import { useEffect, useMemo, useRef, useState } from 'react';
import type { CreateProductBody, Product } from '@/lib/types';
import { agruparCategorias } from '@/lib/categorias';
import { errorMessage } from '@/lib/api';

type Props = {
  initial?: Product;
  /** Categorías ya usadas por otros productos, para no perder ninguna al editar. */
  categoriasEnUso?: string[];
  /** Token del vendedor, necesario para subir la imagen. */
  token?: string | null;
  submitting: boolean;
  error: string | null;
  onSubmit: (body: CreateProductBody) => void;
  onCancel: () => void;
};

const TAMANO_MAXIMO = 3 * 1024 * 1024;

export function ProductForm({
  initial,
  categoriasEnUso = [],
  token,
  submitting,
  error,
  onSubmit,
  onCancel,
}: Props) {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [price, setPrice] = useState('');
  const [stock, setStock] = useState('10');
  const [category, setCategory] = useState('');
  const [imageUrl, setImageUrl] = useState('');
  const [currency, setCurrency] = useState('USD');

  const [subiendo, setSubiendo] = useState(false);
  const [errorImagen, setErrorImagen] = useState<string | null>(null);
  const inputArchivo = useRef<HTMLInputElement>(null);

  const grupos = useMemo(() => agruparCategorias(categoriasEnUso), [categoriasEnUso]);
  const totalCategorias = grupos.reduce((total, grupo) => total + grupo.opciones.length, 0);

  useEffect(() => {
    setName(initial?.name ?? '');
    setDescription(initial?.description ?? '');
    setPrice(initial ? String(initial.price) : '');
    setStock(initial ? String(initial.stockQuantity) : '10');
    setCategory(initial?.category ?? '');
    setImageUrl(initial?.imageUrl ?? '');
    setCurrency(initial?.currencyCode ?? 'USD');
    setErrorImagen(null);
  }, [initial]);

  async function subirImagen(archivo: File) {
    setErrorImagen(null);

    if (archivo.size > TAMANO_MAXIMO) {
      setErrorImagen(
        `La imagen pesa ${(archivo.size / 1024 / 1024).toFixed(1)} MB y el máximo es 3 MB.`,
      );
      return;
    }

    setSubiendo(true);
    try {
      const datos = new FormData();
      datos.append('file', archivo);

      const respuesta = await fetch('/api/upload', {
        method: 'POST',
        headers: token ? { Authorization: `Bearer ${token}` } : undefined,
        body: datos,
      });

      const cuerpo = await respuesta.json().catch(() => null);

      if (!respuesta.ok) {
        throw new Error(cuerpo?.detail ?? `No se pudo subir la imagen (HTTP ${respuesta.status})`);
      }

      setImageUrl(cuerpo.url as string);
    } catch (err) {
      setErrorImagen(errorMessage(err));
    } finally {
      setSubiendo(false);
      if (inputArchivo.current) {
        inputArchivo.current.value = '';
      }
    }
  }

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
            placeholder="Audifonos inalambricos Pro"
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
            placeholder="Caracteristicas del producto…"
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
            <option value="COP">COP</option>
            <option value="EUR">EUR</option>
            <option value="MXN">MXN</option>
          </select>
        </div>

        <div>
          <label className="label" htmlFor="category">
            Categoría *
          </label>
          <select
            id="category"
            className="select"
            value={category}
            onChange={(event) => setCategory(event.target.value)}
            required
          >
            <option value="" disabled>
              Selecciona una categoría…
            </option>
            {grupos.map((grupo) => (
              <optgroup key={grupo.grupo} label={grupo.grupo}>
                {grupo.opciones.map((opcion) => (
                  <option key={opcion} value={opcion}>
                    {opcion === grupo.grupo ? `${opcion} (general)` : opcion}
                  </option>
                ))}
              </optgroup>
            ))}
          </select>
          <p className="mt-1 text-[11px] text-ink-400">{totalCategorias} categorías disponibles</p>
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
          {initial && <p className="mt-1 text-[11px] text-ink-400">Se gestiona desde Inventario</p>}
        </div>

        {/* ---------------- Imagen del producto ---------------- */}
        <div className="sm:col-span-2">
          <span className="label">Imagen del producto</span>

          <div className="flex flex-wrap items-start gap-4">
            <div className="grid h-28 w-28 shrink-0 place-items-center overflow-hidden rounded-lg border border-ink-200 bg-ink-50 text-3xl text-ink-300">
              {imageUrl ? (
                // eslint-disable-next-line @next/next/no-img-element
                <img src={imageUrl} alt="Vista previa" className="h-full w-full object-cover" />
              ) : (
                <span>📦</span>
              )}
            </div>

            <div className="min-w-[220px] flex-1">
              <input
                ref={inputArchivo}
                id="imageFile"
                type="file"
                accept="image/jpeg,image/png,image/webp,image/gif"
                className="input"
                disabled={subiendo}
                onChange={(event) => {
                  const archivo = event.target.files?.[0];
                  if (archivo) void subirImagen(archivo);
                }}
              />
              <p className="mt-1 text-[11px] text-ink-400">
                JPG, PNG, WebP o GIF · máximo 3 MB. La imagen se sube al seleccionarla.
              </p>

              {subiendo && <p className="mt-1 text-xs text-brand-700">Subiendo imagen…</p>}

              {imageUrl && !subiendo && (
                <div className="mt-2 flex items-center gap-2">
                  <span className="truncate font-mono text-[11px] text-ink-500">{imageUrl}</span>
                  <button
                    type="button"
                    className="text-xs text-red-600 hover:underline"
                    onClick={() => {
                      setImageUrl('');
                      setErrorImagen(null);
                    }}
                  >
                    Quitar
                  </button>
                </div>
              )}

              {errorImagen && <p className="mt-2 text-xs text-red-600">{errorImagen}</p>}

              <details className="mt-2">
                <summary className="cursor-pointer text-[11px] text-ink-500">
                  O pega una URL externa
                </summary>
                <input
                  className="input mt-2"
                  value={imageUrl}
                  onChange={(event) => setImageUrl(event.target.value)}
                  placeholder="https://…"
                />
              </details>
            </div>
          </div>
        </div>
      </div>

      {error && (
        <div className="mt-4 rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-800">
          {error}
        </div>
      )}

      <div className="mt-4 flex gap-2">
        <button type="submit" className="btn btn-primary" disabled={submitting || subiendo}>
          {submitting ? 'Guardando…' : initial ? 'Guardar cambios' : 'Publicar producto'}
        </button>
        <button type="button" className="btn btn-secondary" onClick={onCancel}>
          Cancelar
        </button>
      </div>
    </form>
  );
}

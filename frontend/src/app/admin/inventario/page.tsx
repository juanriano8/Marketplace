'use client';

import { useCallback, useEffect, useState } from 'react';
import { apiFetch, errorMessage, type Page } from '@/lib/api';
import { useAuth } from '@/lib/auth';
import type { StockMovement } from '@/lib/types';
import { RequireAuth } from '@/components/RequireAuth';
import { EmptyState, ErrorBox, Loading, PageHeader } from '@/components/feedback';
import { StatusBadge, formatDate } from '@/components/ui';

export default function AdminInventoryAuditPage() {
  return (
    <RequireAuth role="ROLE_ADMIN">
      <Audit />
    </RequireAuth>
  );
}

const TYPE_LABEL: Record<StockMovement['movementType'], string> = {
  INITIAL: 'Inicial',
  ADJUSTMENT: 'Ajuste manual',
  RESERVATION: 'Reserva',
  SALE: 'Venta',
  CANCELLATION: 'Cancelación',
  ADMIN_CORRECTION: 'Corrección admin',
};

function Audit() {
  const { session } = useAuth();
  const [movements, setMovements] = useState<StockMovement[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [productFilter, setProductFilter] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(
    async (productId?: string) => {
      if (!session?.token) return;
      setLoading(true);
      setError(null);
      try {
        const filter = productId ? `productId=${productId}&` : '';
        const result = await apiFetch<Page<StockMovement>>(
          `/api/v1/admin/inventory/audit?${filter}page=${page}&size=50&sort=createdAt,desc`,
          { token: session.token },
        );
        setMovements(result.content);
        setTotal(result.totalElements);
        setTotalPages(result.totalPages);
      } catch (err) {
        setError(errorMessage(err));
      } finally {
        setLoading(false);
      }
    },
    [session?.token, page],
  );

  useEffect(() => {
    void load(productFilter.trim() || undefined);
  }, [load, productFilter]);

  return (
    <div>
      <PageHeader
        title="Auditoría de inventario"
        description={`Ledger append-only con ${total} movimiento(s) registrados`}
        action={
          <button
            type="button"
            className="btn btn-secondary btn-sm"
            onClick={() => void load(productFilter.trim() || undefined)}
          >
            Actualizar
          </button>
        }
      />

      <div className="mb-4 card p-4">
        <label className="label" htmlFor="productFilter">
          Filtrar por ID de producto (opcional)
        </label>
        <input
          id="productFilter"
          className="input"
          value={productFilter}
          onChange={(event) => {
            setPage(0);
            setProductFilter(event.target.value);
          }}
          placeholder="c3d4e5f6-7a8b-9c0d-1e2f-3a4b5c6d7e8f"
        />
      </div>

      {error && (
        <div className="mb-4">
          <ErrorBox>{error}</ErrorBox>
        </div>
      )}

      {loading ? (
        <Loading />
      ) : movements.length === 0 ? (
        <EmptyState
          title="No hay movimientos de stock"
          description="Cada ajuste, reserva o venta queda registrado aquí."
        />
      ) : (
        <>
          <div className="card overflow-x-auto">
            <table className="table">
              <thead>
                <tr>
                  <th className="w-40">Tipo</th>
                  <th className="w-32">Producto</th>
                  <th className="w-24">Antes</th>
                  <th className="w-24">Cambio</th>
                  <th className="w-24">Después</th>
                  <th>Motivo</th>
                  <th className="w-40">Fecha</th>
                </tr>
              </thead>
              <tbody>
                {movements.map((movement) => (
                  <tr key={movement.movementId}>
                    <td>
                      <StatusBadge status={movement.movementType} />
                      <p className="mt-0.5 text-[11px] text-ink-400">
                        {TYPE_LABEL[movement.movementType]}
                      </p>
                    </td>
                    <td className="font-mono text-xs text-ink-500">
                      {movement.productId.slice(0, 8)}…
                    </td>
                    <td className="text-ink-500">{movement.quantityBefore}</td>
                    <td
                      className={`font-semibold ${
                        movement.quantityDelta > 0
                          ? 'text-green-700'
                          : movement.quantityDelta < 0
                            ? 'text-red-600'
                            : 'text-ink-400'
                      }`}
                    >
                      {movement.quantityDelta > 0 ? `+${movement.quantityDelta}` : movement.quantityDelta}
                    </td>
                    <td className="font-semibold">{movement.quantityAfter}</td>
                    <td className="text-xs text-ink-600">
                      {movement.reason ?? '—'}
                      {movement.reference && (
                        <span className="ml-1 font-mono text-[11px] text-ink-400">
                          ({movement.reference})
                        </span>
                      )}
                    </td>
                    <td className="text-xs text-ink-500">{formatDate(movement.occurredAt)}</td>
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
        </>
      )}
    </div>
  );
}

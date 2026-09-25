'use client';

import { useCallback, useEffect, useState } from 'react';
import { apiFetch, errorMessage, type Page } from '@/lib/api';
import { useAuth } from '@/lib/auth';
import type { UserResponse } from '@/lib/types';
import { RequireAuth } from '@/components/RequireAuth';
import { EmptyState, ErrorBox, Loading, PageHeader, Success, Warning } from '@/components/feedback';
import { formatDate } from '@/components/ui';

type Filter = 'pending' | 'approved' | 'all';

export default function AdminSellersPage() {
  return (
    <RequireAuth role="ROLE_ADMIN">
      <SellerVerification />
    </RequireAuth>
  );
}

function SellerVerification() {
  const { session } = useAuth();
  const [filter, setFilter] = useState<Filter>('pending');
  const [sellers, setSellers] = useState<UserResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [feedback, setFeedback] = useState<string | null>(null);
  const [busy, setBusy] = useState<string | null>(null);

  const load = useCallback(async () => {
    if (!session?.token) return;
    setLoading(true);
    setError(null);
    try {
      const query = filter === 'all' ? '' : `approved=${filter === 'approved'}&`;
      const page = await apiFetch<Page<UserResponse>>(
        `/api/v1/admin/sellers?${query}page=0&size=100&sort=createdAt,desc`,
        { token: session.token },
      );
      setSellers(page.content);
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [session?.token, filter]);

  useEffect(() => {
    void load();
  }, [load]);

  async function verify(seller: UserResponse, approved: boolean) {
    if (!session?.token) return;
    setBusy(seller.id);
    setError(null);
    setFeedback(null);
    try {
      await apiFetch<UserResponse>(`/api/v1/admin/sellers/${seller.id}/verify`, {
        method: 'PATCH',
        token: session.token,
        body: { approved },
      });
      setFeedback(
        approved
          ? `${seller.email} verificado: ya puede publicar productos.`
          : `${seller.email} rechazado y deshabilitado.`,
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
        title="Vendedores"
        description="Verifica las cuentas de vendedor para que puedan publicar en el catálogo."
        action={
          <button type="button" className="btn btn-secondary btn-sm" onClick={() => void load()}>
            Actualizar
          </button>
        }
      />

      <div className="mb-4 flex flex-wrap gap-2">
        {(
          [
            { value: 'pending', label: 'Pendientes' },
            { value: 'approved', label: 'Verificados' },
            { value: 'all', label: 'Todos' },
          ] as const
        ).map((option) => (
          <button
            key={option.value}
            type="button"
            className={`btn btn-sm ${filter === option.value ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => setFilter(option.value)}
          >
            {option.label}
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

      {filter === 'pending' && sellers.length > 0 && (
        <div className="mb-4">
          <Warning>
            Rechazar un vendedor <strong>deshabilita su cuenta</strong>: no podrá iniciar sesión.
          </Warning>
        </div>
      )}

      {loading ? (
        <Loading />
      ) : sellers.length === 0 ? (
        <EmptyState
          title={
            filter === 'pending'
              ? 'No hay vendedores pendientes de verificación'
              : 'No hay vendedores en esta lista'
          }
          description="Cuando alguien se registre como vendedor aparecerá aquí para que lo apruebes."
        />
      ) : (
        <div className="card overflow-x-auto">
          <table className="table">
            <thead>
              <tr>
                <th>Correo</th>
                <th className="w-40">Registrado</th>
                <th className="w-32">Verificación</th>
                <th className="w-24">Cuenta</th>
                <th className="w-48" />
              </tr>
            </thead>
            <tbody>
              {sellers.map((seller) => (
                <tr key={seller.id}>
                  <td>
                    <p className="font-medium text-ink-900">{seller.email}</p>
                    <p className="font-mono text-[11px] text-ink-400">{seller.id.slice(0, 8)}…</p>
                  </td>
                  <td className="text-xs text-ink-500">{formatDate(seller.createdAt)}</td>
                  <td>
                    {seller.sellerApproved === true ? (
                      <span className="badge badge-green">Verificado</span>
                    ) : (
                      <span className="badge badge-yellow">Pendiente</span>
                    )}
                  </td>
                  <td>
                    {seller.enabled ? (
                      <span className="badge badge-gray">Activa</span>
                    ) : (
                      <span className="badge badge-red">Deshabilitada</span>
                    )}
                  </td>
                  <td>
                    {seller.sellerApproved === true ? (
                      <button
                        type="button"
                        className="btn btn-danger btn-sm"
                        disabled={busy === seller.id}
                        onClick={() => void verify(seller, false)}
                      >
                        Revocar
                      </button>
                    ) : (
                      <div className="flex gap-2">
                        <button
                          type="button"
                          className="btn btn-success btn-sm"
                          disabled={busy === seller.id}
                          onClick={() => void verify(seller, true)}
                        >
                          Verificar
                        </button>
                        <button
                          type="button"
                          className="btn btn-danger btn-sm"
                          disabled={busy === seller.id}
                          onClick={() => void verify(seller, false)}
                        >
                          Rechazar
                        </button>
                      </div>
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
